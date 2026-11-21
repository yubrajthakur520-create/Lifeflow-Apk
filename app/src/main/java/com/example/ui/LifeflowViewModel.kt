package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.HabitRepository
import com.example.data.local.LifeflowDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.domain.HabitAnalyticsEngine
import com.example.model.AnalyticsSummary
import com.example.model.AppDate
import com.example.model.DashboardSummary
import com.example.model.Habit
import com.example.model.HabitCategory
import com.example.model.HabitCompletion
import com.example.model.HabitStats
import com.example.model.NotificationPreferences
import com.example.model.RoutinePeriod
import com.example.model.ThemeMode
import com.example.model.UserPreferences
import com.example.model.WeekStartDay
import com.example.notifications.HabitReminderScheduler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HabitStatusFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    PAUSED("Paused")
}

data class LifeflowUiState(
    val isLoading: Boolean = true,
    val today: AppDate = AppDate.today(),
    val preferences: UserPreferences = UserPreferences(),
    val habits: List<Habit> = emptyList(),
    val completions: List<HabitCompletion> = emptyList(),
    val dashboardSummary: DashboardSummary = HabitAnalyticsEngine.buildDashboardSummary(emptyList(), emptyList()),
    val analyticsSummary: AnalyticsSummary = HabitAnalyticsEngine.buildAnalyticsSummary(emptyList(), emptyList()),
    val habitStatsMap: Map<String, HabitStats> = emptyMap(),
    val todayCompletedIds: Set<String> = emptySet(),
    val selectedRoutineFilter: RoutinePeriod? = null,
    val searchQuery: String = "",
    val selectedCategoryFilter: HabitCategory? = null,
    val selectedStatusFilter: HabitStatusFilter = HabitStatusFilter.ALL,
    val selectedCalendarDate: AppDate = AppDate.today(),
    val displayedMonth: AppDate = AppDate.today().firstDayOfMonth()
)

sealed interface UiEvent {
    data class ShowMessage(val message: String) : UiEvent
    data class ExportReady(val jsonPayload: String) : UiEvent
}

private data class UiFilters(
    val selectedRoutineFilter: RoutinePeriod? = null,
    val searchQuery: String = "",
    val selectedCategoryFilter: HabitCategory? = null,
    val selectedStatusFilter: HabitStatusFilter = HabitStatusFilter.ALL,
    val selectedCalendarDate: AppDate = AppDate.today(),
    val displayedMonth: AppDate = AppDate.today().firstDayOfMonth()
)

class LifeflowViewModel(
    private val appContext: Context,
    private val repository: HabitRepository,
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    private val filters = MutableStateFlow(UiFilters())
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    val uiState: StateFlow<LifeflowUiState> = combine(
        repository.habitsFlow,
        repository.completionsFlow,
        repository.preferencesFlow,
        filters
    ) { habits, completions, prefs, currentFilters ->
        val today = AppDate.today()
        val dashboardSummary = HabitAnalyticsEngine.buildDashboardSummary(habits, completions, today)
        val analyticsSummary = HabitAnalyticsEngine.buildAnalyticsSummary(habits, completions, today)
        val statsMap = habits.associate { habit ->
            habit.id to HabitAnalyticsEngine.calculateHabitStats(habit, completions, today)
        }
        val todayCompleted = HabitAnalyticsEngine.completedHabitIdsOnDate(completions, today)

        LifeflowUiState(
            isLoading = false,
            today = today,
            preferences = prefs,
            habits = habits,
            completions = completions,
            dashboardSummary = dashboardSummary,
            analyticsSummary = analyticsSummary,
            habitStatsMap = statsMap,
            todayCompletedIds = todayCompleted,
            selectedRoutineFilter = currentFilters.selectedRoutineFilter,
            searchQuery = currentFilters.searchQuery,
            selectedCategoryFilter = currentFilters.selectedCategoryFilter,
            selectedStatusFilter = currentFilters.selectedStatusFilter,
            selectedCalendarDate = currentFilters.selectedCalendarDate,
            displayedMonth = currentFilters.displayedMonth
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = LifeflowUiState()
    )

    init {
        viewModelScope.launch {
            try {
                repository.ensureStarterRoutinesSeeded()
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowMessage("Unable to initialize routines: ${e.message}"))
            }
        }
    }

    fun selectRoutineFilter(period: RoutinePeriod?) {
        filters.value = filters.value.copy(selectedRoutineFilter = period)
    }

    fun updateSearchQuery(query: String) {
        filters.value = filters.value.copy(searchQuery = query)
    }

    fun selectCategoryFilter(category: HabitCategory?) {
        filters.value = filters.value.copy(selectedCategoryFilter = category)
    }

    fun selectStatusFilter(status: HabitStatusFilter) {
        filters.value = filters.value.copy(selectedStatusFilter = status)
    }

    fun selectCalendarDate(date: AppDate) {
        filters.value = filters.value.copy(selectedCalendarDate = date)
    }

    fun navigateMonth(deltaMonths: Int) {
        val next = filters.value.displayedMonth.plusMonths(deltaMonths).firstDayOfMonth()
        filters.value = filters.value.copy(displayedMonth = next)
    }

    fun jumpCalendarToToday() {
        val today = AppDate.today()
        filters.value = filters.value.copy(
            selectedCalendarDate = today,
            displayedMonth = today.firstDayOfMonth()
        )
    }

    fun toggleHabitCompletion(habitId: String, date: AppDate, currentlyCompleted: Boolean) {
        val today = AppDate.today()
        if (date > today) {
            viewModelScope.launch {
                _events.emit(UiEvent.ShowMessage("Future dates cannot be marked complete yet."))
            }
            return
        }
        viewModelScope.launch {
            try {
                repository.setHabitCompleted(habitId, date, !currentlyCompleted)
                syncReminders()
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowMessage("Failed to update habit record: ${e.message}"))
            }
        }
    }

    fun saveHabit(habit: Habit, isEdit: Boolean) {
        viewModelScope.launch {
            try {
                if (isEdit) {
                    repository.updateHabit(habit)
                    _events.emit(UiEvent.ShowMessage("Updated \"${habit.title}\""))
                } else {
                    repository.createHabit(habit)
                    _events.emit(UiEvent.ShowMessage("Added \"${habit.title}\" to your schedule"))
                }
                syncReminders()
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowMessage("Could not save habit: ${e.message}"))
            }
        }
    }

    fun toggleHabitActive(habit: Habit) {
        viewModelScope.launch {
            try {
                val newActive = !habit.isActive
                repository.setHabitActive(habit.id, newActive)
                syncReminders()
                _events.emit(
                    UiEvent.ShowMessage(
                        if (newActive) "\"${habit.title}\" resumed" else "\"${habit.title}\" paused"
                    )
                )
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowMessage("Could not update habit status: ${e.message}"))
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                HabitReminderScheduler.cancelHabitReminder(appContext, habit.id)
                repository.deleteHabit(habit.id)
                _events.emit(UiEvent.ShowMessage("Deleted \"${habit.title}\""))
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowMessage("Could not delete habit: ${e.message}"))
            }
        }
    }

    fun updateProfile(name: String, email: String, profileImageUri: String?) {
        viewModelScope.launch {
            try {
                prefsRepo.updateProfile(name, email, profileImageUri)
                _events.emit(UiEvent.ShowMessage("Profile saved"))
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowMessage("Could not save profile: ${e.message}"))
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            prefsRepo.setThemeMode(mode)
        }
    }

    fun setWeekStartDay(day: WeekStartDay) {
        viewModelScope.launch {
            prefsRepo.setWeekStartDay(day)
        }
    }

    fun updateNotificationPreferences(notifications: NotificationPreferences) {
        viewModelScope.launch {
            prefsRepo.updateNotificationPreferences(notifications)
            HabitReminderScheduler.syncAllReminders(
                appContext,
                uiState.value.habits,
                notifications
            )
        }
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            val sent = HabitReminderScheduler.showImmediateNotification(
                context = appContext,
                notificationId = 9999,
                title = "Lifeflow Reminders Active",
                message = "Small Habits. Better Days. Stronger You. Your reminders are ready!"
            )
            _events.emit(
                UiEvent.ShowMessage(
                    if (sent) "Test notification sent!"
                    else "Notification permission required. Tap Grant Permission to enable alerts."
                )
            )
        }
    }

    fun restoreStarterRoutines() {
        viewModelScope.launch {
            try {
                repository.restoreStarterRoutines()
                _events.emit(UiEvent.ShowMessage("Starter daily routines restored"))
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowMessage("Failed to restore starter routines: ${e.message}"))
            }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            try {
                val json = repository.buildExportJson()
                _events.emit(UiEvent.ExportReady(json))
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowMessage("Export failed: ${e.message}"))
            }
        }
    }

    fun resetLocalData() {
        viewModelScope.launch {
            try {
                repository.resetAllData()
                syncReminders()
                _events.emit(UiEvent.ShowMessage("Local profile and habits reset to defaults"))
            } catch (e: Exception) {
                _events.emit(UiEvent.ShowMessage("Reset failed: ${e.message}"))
            }
        }
    }

    private fun syncReminders() {
        val state = uiState.value
        HabitReminderScheduler.syncAllReminders(
            appContext,
            state.habits,
            state.preferences.notifications
        )
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val appCtx = context.applicationContext
            val db = LifeflowDatabase.getInstance(appCtx)
            val prefsRepo = UserPreferencesRepository(appCtx)
            val repository = HabitRepository(db.habitDao(), prefsRepo)
            return LifeflowViewModel(appCtx, repository, prefsRepo) as T
        }
    }
}
