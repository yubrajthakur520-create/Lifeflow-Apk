package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.HabitRepository
import com.example.data.local.LifeflowDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.model.AppDate
import com.example.model.Habit
import com.example.model.HabitCategory
import com.example.model.HabitFrequency
import com.example.model.HabitIcon
import com.example.model.RoutinePeriod
import com.example.model.ThemeMode
import com.example.model.WeekStartDay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HabitRepositoryTest {

    private lateinit var context: Context
    private lateinit var database: LifeflowDatabase
    private lateinit var prefsRepo: UserPreferencesRepository
    private lateinit var repository: HabitRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, LifeflowDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        prefsRepo = UserPreferencesRepository(context)
        repository = HabitRepository(database.habitDao(), prefsRepo)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun starterRoutinesSeedOnceWithoutDuplicates() = runTest {
        prefsRepo.resetProfileAndPreferences()
        repository.ensureStarterRoutinesSeeded()
        val firstPass = repository.habitsFlow.first()
        assertEquals(19, firstPass.size)

        // Calling again should not duplicate starter routines
        repository.ensureStarterRoutinesSeeded()
        val secondPass = repository.habitsFlow.first()
        assertEquals(19, secondPass.size)
    }

    @Test
    fun preventsDuplicateCompletionsForSameHabitAndDateAndSupportsUndo() = runTest {
        val customHabit = Habit(
            id = "custom_1",
            title = "Deep Focus",
            description = "90 minute coding block",
            category = HabitCategory.PRODUCTIVITY,
            routinePeriod = RoutinePeriod.MORNING,
            icon = HabitIcon.PSYCHOLOGY,
            frequency = HabitFrequency.DAILY,
            scheduledDays = setOf(1, 2, 3, 4, 5, 6, 7),
            reminderTime = "09:00",
            reminderEnabled = false,
            startDate = AppDate(2026, 11, 1),
            targetDurationMinutes = 90,
            isActive = true,
            isStarterRoutine = false,
            sortOrder = 0,
            createdAtEpochMs = 1L,
            updatedAtEpochMs = 1L
        )
        repository.createHabit(customHabit)

        val date = AppDate(2026, 11, 17)
        // Complete twice on the same date -> must store only 1 record
        repository.setHabitCompleted("custom_1", date, true)
        repository.setHabitCompleted("custom_1", date, true)

        val completionsAfterComplete = repository.completionsFlow.first()
        assertEquals(1, completionsAfterComplete.count { it.habitId == "custom_1" && it.date == date })

        // Undo completion -> record removed
        repository.setHabitCompleted("custom_1", date, false)
        val completionsAfterUndo = repository.completionsFlow.first()
        assertTrue(completionsAfterUndo.none { it.habitId == "custom_1" && it.date == date })
    }

    @Test
    fun pauseResumeDeleteAndExportJsonWorkEndToEnd() = runTest {
        val habit = repository.createHabit(
            Habit(
                id = "h_export",
                title = "Evening Meditation",
                description = "Calm breathing",
                category = HabitCategory.MINDFULNESS,
                routinePeriod = RoutinePeriod.EVENING,
                icon = HabitIcon.SELF_IMPROVEMENT,
                frequency = HabitFrequency.DAILY,
                scheduledDays = setOf(1, 2, 3, 4, 5, 6, 7),
                reminderTime = "20:00",
                reminderEnabled = true,
                startDate = AppDate(2026, 11, 10),
                targetDurationMinutes = 15,
                isActive = true,
                isStarterRoutine = false,
                sortOrder = 0,
                createdAtEpochMs = 1L,
                updatedAtEpochMs = 1L
            )
        )

        repository.setHabitActive(habit.id, false)
        assertFalse(repository.habitsFlow.first().first { it.id == habit.id }.isActive)

        repository.setHabitActive(habit.id, true)
        assertTrue(repository.habitsFlow.first().first { it.id == habit.id }.isActive)

        prefsRepo.setThemeMode(ThemeMode.DARK)
        prefsRepo.setWeekStartDay(WeekStartDay.SUNDAY)
        repository.setHabitCompleted(habit.id, AppDate(2026, 11, 17), true)

        val jsonString = repository.buildExportJson()
        val parsed = JSONObject(jsonString)
        assertEquals("Lifeflow", parsed.getString("app"))
        assertEquals("DARK", parsed.getJSONObject("userPreferences").getString("themeMode"))
        assertEquals("SUNDAY", parsed.getJSONObject("userPreferences").getString("weekStartDay"))
        assertTrue(parsed.getJSONArray("habits").length() >= 1)
        assertTrue(parsed.getJSONArray("habitCompletions").length() >= 1)

        // Deleting habit cascades to completions
        repository.deleteHabit(habit.id)
        assertTrue(repository.habitsFlow.first().none { it.id == habit.id })
        assertTrue(repository.completionsFlow.first().none { it.habitId == habit.id })
    }
}
