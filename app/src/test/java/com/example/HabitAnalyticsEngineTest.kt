package com.example

import com.example.domain.HabitAnalyticsEngine
import com.example.model.AppDate
import com.example.model.Habit
import com.example.model.HabitCategory
import com.example.model.HabitCompletion
import com.example.model.HabitFrequency
import com.example.model.HabitIcon
import com.example.model.RoutinePeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitAnalyticsEngineTest {

    private fun sampleHabit(
        id: String = "h1",
        startDate: AppDate = AppDate(2026, 11, 10),
        days: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
        isActive: Boolean = true
    ) = Habit(
        id = id,
        title = "Drink water",
        description = "Hydrate",
        category = HabitCategory.HEALTH,
        routinePeriod = RoutinePeriod.MORNING,
        icon = HabitIcon.WATER_DROP,
        frequency = HabitFrequency.DAILY,
        scheduledDays = days,
        reminderTime = "07:00",
        reminderEnabled = false,
        startDate = startDate,
        targetDurationMinutes = null,
        isActive = isActive,
        isStarterRoutine = true,
        sortOrder = 0,
        createdAtEpochMs = 1L,
        updatedAtEpochMs = 1L
    )

    @Test
    fun calculatesHabitStreaksAndRateAccurately() {
        val start = AppDate(2026, 11, 10)
        val today = AppDate(2026, 11, 14) // 5 days: 10, 11, 12, 13, 14
        val habit = sampleHabit(startDate = start)

        val completions = listOf(
            HabitCompletion("h1_2026-11-10", "h1", AppDate(2026, 11, 10), true, 1L),
            HabitCompletion("h1_2026-11-11", "h1", AppDate(2026, 11, 11), true, 1L),
            // 12 missed
            HabitCompletion("h1_2026-11-13", "h1", AppDate(2026, 11, 13), true, 1L),
            HabitCompletion("h1_2026-11-14", "h1", AppDate(2026, 11, 14), true, 1L)
        )

        val stats = HabitAnalyticsEngine.calculateHabitStats(habit, completions, today)
        assertEquals(2, stats.currentStreak)
        assertEquals(2, stats.longestStreak)
        assertEquals(5, stats.scheduledDaysCount)
        assertEquals(4, stats.totalCompletions)
        assertEquals(0.8f, stats.completionRate, 0.001f)
    }

    @Test
    fun todayInProgressDoesNotBreakCurrentStreak() {
        val start = AppDate(2026, 11, 10)
        val today = AppDate(2026, 11, 13)
        val habit = sampleHabit(startDate = start)

        val completions = listOf(
            HabitCompletion("h1_2026-11-10", "h1", AppDate(2026, 11, 10), true, 1L),
            HabitCompletion("h1_2026-11-11", "h1", AppDate(2026, 11, 11), true, 1L),
            HabitCompletion("h1_2026-11-12", "h1", AppDate(2026, 11, 12), true, 1L)
        )

        val stats = HabitAnalyticsEngine.calculateHabitStats(habit, completions, today)
        assertEquals(3, stats.currentStreak)
        assertEquals(3, stats.longestStreak)
    }

    @Test
    fun weekdayMaskFiltersScheduleCorrectly() {
        // 2026-11-17 is Tuesday (isoDayOfWeek = 2), 2026-11-14 is Saturday (6)
        val tuesday = AppDate(2026, 11, 17)
        val saturday = AppDate(2026, 11, 14)
        val weekdayHabit = sampleHabit(
            startDate = AppDate(2026, 11, 1),
            days = setOf(1, 2, 3, 4, 5)
        )
        assertTrue(HabitAnalyticsEngine.isHabitScheduledOn(weekdayHabit, tuesday))
        assertFalse(HabitAnalyticsEngine.isHabitScheduledOn(weekdayHabit, saturday))
    }

    @Test
    fun dashboardAndAnalyticsReflectRealCompletions() {
        val today = AppDate(2026, 11, 17)
        val h1 = sampleHabit(id = "h1", startDate = today.minusDays(2))
        val h2 = sampleHabit(id = "h2", startDate = today.minusDays(2))

        val completions = listOf(
            HabitCompletion("h1_${today.toIsoString()}", "h1", today, true, 1L)
        )

        val dash = HabitAnalyticsEngine.buildDashboardSummary(
            habits = listOf(h1, h2),
            completions = completions,
            today = today,
            hourOfDay = 9
        )
        assertEquals("Good Morning", dash.greeting)
        assertEquals(2, dash.totalScheduledToday)
        assertEquals(1, dash.completedToday)
        assertEquals(1, dash.remainingToday)
        assertEquals(50, dash.completionPercentToday)

        val analytics = HabitAnalyticsEngine.buildAnalyticsSummary(listOf(h1, h2), completions, today)
        assertEquals(1, analytics.totalCompletedAllTime)
        assertEquals(7, analytics.weeklyProgress.size)
        assertEquals(30, analytics.monthlyProgress.size)
    }
}
