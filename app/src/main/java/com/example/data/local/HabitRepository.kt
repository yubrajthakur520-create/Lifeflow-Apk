package com.example.data.local

import com.example.model.AppDate
import com.example.model.Habit
import com.example.model.HabitCompletion
import com.example.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class HabitRepository(
    private val habitDao: HabitDao,
    private val prefsRepo: UserPreferencesRepository
) {
    val habitsFlow: Flow<List<Habit>> = habitDao.observeAllHabits().map { list ->
        list.map { it.toDomain() }
    }

    val completionsFlow: Flow<List<HabitCompletion>> = habitDao.observeAllCompletions().map { list ->
        list.map { it.toDomain() }
    }

    val preferencesFlow: Flow<UserPreferences> = prefsRepo.preferencesFlow

    suspend fun ensureStarterRoutinesSeeded() {
        val prefs = prefsRepo.preferencesFlow.first()
        if (!prefs.hasSeededStarterRoutines) {
            val existing = habitDao.getAllHabitsOnce()
            if (existing.isEmpty()) {
                habitDao.upsertHabits(StarterRoutines.createStarterEntities())
            }
            prefsRepo.markStarterRoutinesSeeded()
        }
    }

    suspend fun restoreStarterRoutines() {
        val existingTitles = habitDao.getAllHabitsOnce().map { it.title.lowercase() }.toSet()
        val starters = StarterRoutines.createStarterEntities()
            .filter { it.title.lowercase() !in existingTitles }
        if (starters.isNotEmpty()) {
            habitDao.upsertHabits(starters)
        }
        prefsRepo.markStarterRoutinesSeeded()
    }

    suspend fun createHabit(habit: Habit): Habit {
        val now = System.currentTimeMillis()
        val existingCount = habitDao.getAllHabitsOnce().size
        val newHabit = habit.copy(
            id = habit.id.ifBlank { UUID.randomUUID().toString() },
            sortOrder = existingCount,
            createdAtEpochMs = now,
            updatedAtEpochMs = now
        )
        habitDao.upsertHabit(HabitEntity.fromDomain(newHabit))
        return newHabit
    }

    suspend fun updateHabit(habit: Habit): Habit {
        val updated = habit.copy(updatedAtEpochMs = System.currentTimeMillis())
        habitDao.upsertHabit(HabitEntity.fromDomain(updated))
        return updated
    }

    suspend fun setHabitActive(habitId: String, active: Boolean) {
        habitDao.setHabitActive(habitId, active, System.currentTimeMillis())
    }

    suspend fun deleteHabit(habitId: String) {
        habitDao.deleteHabitById(habitId)
    }

    /**
     * Toggles or explicitly sets completion for (habitId, date).
     * Uses deterministic composite ID `${habitId}_${isoDate}` + unique SQLite index
     * to prevent duplicate completion records.
     */
    suspend fun setHabitCompleted(habitId: String, date: AppDate, completed: Boolean) {
        val isoDate = date.toIsoString()
        if (completed) {
            val record = HabitCompletionEntity(
                id = "${habitId}_$isoDate",
                habitId = habitId,
                date = isoDate,
                completed = true,
                completedAtEpochMs = System.currentTimeMillis()
            )
            habitDao.upsertCompletion(record)
        } else {
            habitDao.deleteCompletion(habitId, isoDate)
        }
    }

    suspend fun resetAllData() {
        habitDao.clearAllCompletions()
        habitDao.clearAllHabits()
        prefsRepo.resetProfileAndPreferences()
        habitDao.upsertHabits(StarterRoutines.createStarterEntities())
        prefsRepo.markStarterRoutinesSeeded()
    }

    suspend fun buildExportJson(): String {
        val habits = habitDao.getAllHabitsOnce().map { it.toDomain() }
        val completions = habitDao.getAllCompletionsOnce().map { it.toDomain() }
        val prefs = prefsRepo.preferencesFlow.first()

        val root = JSONObject()
        root.put("app", "Lifeflow")
        root.put("exportedAt", System.currentTimeMillis())

        val profileJson = JSONObject().apply {
            put("name", prefs.profile.name)
            put("email", prefs.profile.email)
            put("themeMode", prefs.themeMode.name)
            put("weekStartDay", prefs.weekStartDay.name)
        }
        root.put("userPreferences", profileJson)

        val habitsArray = JSONArray()
        habits.forEach { habit ->
            habitsArray.put(
                JSONObject().apply {
                    put("id", habit.id)
                    put("title", habit.title)
                    put("description", habit.description)
                    put("category", habit.category.displayName)
                    put("routinePeriod", habit.routinePeriod.displayName)
                    put("frequency", habit.frequency.name)
                    put("scheduledDays", JSONArray(habit.scheduledDays.sorted()))
                    put("reminderTime", habit.reminderTime ?: JSONObject.NULL)
                    put("reminderEnabled", habit.reminderEnabled)
                    put("startDate", habit.startDate.toIsoString())
                    put("targetDurationMinutes", habit.targetDurationMinutes ?: JSONObject.NULL)
                    put("isActive", habit.isActive)
                }
            )
        }
        root.put("habits", habitsArray)

        val completionsArray = JSONArray()
        completions.forEach { c ->
            completionsArray.put(
                JSONObject().apply {
                    put("id", c.id)
                    put("habitId", c.habitId)
                    put("date", c.date.toIsoString())
                    put("completed", c.completed)
                    put("completedAtEpochMs", c.completedAtEpochMs)
                }
            )
        }
        root.put("habitCompletions", completionsArray)
        return root.toString(2)
    }
}
