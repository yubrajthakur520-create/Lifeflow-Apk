package com.example.domain

import com.example.model.AnalyticsSummary
import com.example.model.AppDate
import com.example.model.CategoryProgress
import com.example.model.DailyProgress
import com.example.model.DashboardSummary
import com.example.model.Habit
import com.example.model.HabitCategory
import com.example.model.HabitCompletion
import com.example.model.HabitStats
import com.example.model.MotivationalQuotes
import java.util.Calendar

object HabitAnalyticsEngine {

    fun isHabitScheduledOn(habit: Habit, date: AppDate, includePausedForPast: Boolean = false): Boolean {
        if (!habit.isActive && !includePausedForPast) return false
        if (date < habit.startDate) return false
        return date.isoDayOfWeek in habit.scheduledDays
    }

    /**
     * Returns habits scheduled for [date].
     * For today or future dates, only active habits are shown.
     * For past dates, shows active habits OR paused habits that have completion history or were active since startDate.
     */
    fun scheduledHabitsForDate(
        habits: List<Habit>,
        completions: List<HabitCompletion>,
        date: AppDate,
        today: AppDate = AppDate.today()
    ): List<Habit> {
        val completedIdsOnDate = completions
            .asSequence()
            .filter { it.completed && it.date == date }
            .map { it.habitId }
            .toSet()

        return habits.filter { habit ->
            if (date >= today) {
                isHabitScheduledOn(habit, date, includePausedForPast = false)
            } else {
                isHabitScheduledOn(habit, date, includePausedForPast = false) ||
                    (habit.id in completedIdsOnDate)
            }
        }
    }

    fun completedHabitIdsOnDate(
        completions: List<HabitCompletion>,
        date: AppDate
    ): Set<String> = completions
        .asSequence()
        .filter { it.completed && it.date == date }
        .map { it.habitId }
        .toSet()

    fun dailyProgressForDate(
        habits: List<Habit>,
        completions: List<HabitCompletion>,
        date: AppDate,
        today: AppDate = AppDate.today()
    ): DailyProgress {
        val scheduled = scheduledHabitsForDate(habits, completions, date, today)
        val completedIds = completedHabitIdsOnDate(completions, date)
        val completedCount = scheduled.count { it.id in completedIds }
        return DailyProgress(
            date = date,
            scheduledCount = scheduled.size,
            completedCount = completedCount
        )
    }

    fun calculateHabitStats(
        habit: Habit,
        completions: List<HabitCompletion>,
        today: AppDate = AppDate.today()
    ): HabitStats {
        val completedDates = completions
            .asSequence()
            .filter { it.habitId == habit.id && it.completed }
            .map { it.date }
            .toSet()

        val start = if (habit.startDate <= today) habit.startDate else today
        var cursor = start
        var scheduledCount = 0
        var completedOnSchedule = 0
        var longest = 0
        var running = 0
        val scheduledDates = mutableListOf<AppDate>()

        while (cursor <= today) {
            if (cursor.isoDayOfWeek in habit.scheduledDays) {
                scheduledCount++
                scheduledDates.add(cursor)
                if (cursor in completedDates) {
                    completedOnSchedule++
                    running++
                    if (running > longest) longest = running
                } else {
                    running = 0
                }
            }
            cursor = cursor.plusDays(1)
        }

        // Current streak: count backwards from last scheduled day <= today.
        // If today is scheduled and not yet completed, don't break streak until the day ends;
        // start checking from the previous scheduled day.
        var currentStreak = 0
        for (i in scheduledDates.indices.reversed()) {
            val d = scheduledDates[i]
            if (d in completedDates) {
                currentStreak++
            } else if (d == today && i == scheduledDates.lastIndex) {
                // Today is still in progress
                continue
            } else {
                break
            }
        }

        val rate = if (scheduledCount == 0) 0f else completedOnSchedule.toFloat() / scheduledCount.toFloat()

        return HabitStats(
            habit = habit,
            currentStreak = currentStreak,
            longestStreak = longest,
            completionRate = rate.coerceIn(0f, 1f),
            totalCompletions = completedDates.size,
            scheduledDaysCount = scheduledCount
        )
    }

    /**
     * Computes overall current & longest day-streaks across active habits.
     * A day counts as a successful streak day when all scheduled habits for that day are completed
     * (with at least 1 scheduled habit). Today is treated gracefully while still in progress.
     */
    fun calculateOverallStreaks(
        habits: List<Habit>,
        completions: List<HabitCompletion>,
        today: AppDate = AppDate.today()
    ): Pair<Int, Int> {
        val activeHabits = habits.filter { it.isActive }
        if (activeHabits.isEmpty()) return 0 to 0

        val earliestStart = activeHabits.minOf { it.startDate }
        val scanStart = maxOfDate(earliestStart, today.minusDays(365))

        var cursor = scanStart
        var longest = 0
        var running = 0
        val dayResults = mutableListOf<Pair<AppDate, Boolean>>()

        while (cursor <= today) {
            val progress = dailyProgressForDate(activeHabits, completions, cursor, today)
            if (progress.scheduledCount > 0) {
                val allDone = progress.completedCount >= progress.scheduledCount
                dayResults.add(cursor to allDone)
                if (allDone) {
                    running++
                    if (running > longest) longest = running
                } else {
                    running = 0
                }
            }
            cursor = cursor.plusDays(1)
        }

        var current = 0
        for (i in dayResults.indices.reversed()) {
            val (date, done) = dayResults[i]
            if (done) {
                current++
            } else if (date == today && i == dayResults.lastIndex) {
                continue
            } else {
                break
            }
        }

        return current to longest
    }

    fun buildDashboardSummary(
        habits: List<Habit>,
        completions: List<HabitCompletion>,
        today: AppDate = AppDate.today(),
        hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    ): DashboardSummary {
        val greeting = when (hourOfDay) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
        val todayProgress = dailyProgressForDate(habits, completions, today, today)
        val (currentStreak, _) = calculateOverallStreaks(habits, completions, today)
        return DashboardSummary(
            greeting = greeting,
            today = today,
            totalScheduledToday = todayProgress.scheduledCount,
            completedToday = todayProgress.completedCount,
            remainingToday = (todayProgress.scheduledCount - todayProgress.completedCount).coerceAtLeast(0),
            completionPercentToday = todayProgress.completionPercent,
            currentOverallStreak = currentStreak,
            quote = MotivationalQuotes.quoteForDate(today)
        )
    }

    fun buildAnalyticsSummary(
        habits: List<Habit>,
        completions: List<HabitCompletion>,
        today: AppDate = AppDate.today()
    ): AnalyticsSummary {
        val activeHabits = habits.filter { it.isActive }
        val stats = activeHabits.map { calculateHabitStats(it, completions, today) }
        val (currentStreak, longestStreak) = calculateOverallStreaks(habits, completions, today)

        val weeklyProgress = (6 downTo 0).map { offset ->
            dailyProgressForDate(habits, completions, today.minusDays(offset), today)
        }
        val monthlyProgress = (29 downTo 0).map { offset ->
            dailyProgressForDate(habits, completions, today.minusDays(offset), today)
        }

        val totalCompletedAllTime = completions.count { it.completed }
        val totalScheduled30Days = monthlyProgress.sumOf { it.scheduledCount }
        val totalCompleted30Days = monthlyProgress.sumOf { it.completedCount }
        val overallRate = if (totalScheduled30Days == 0) {
            0f
        } else {
            (totalCompleted30Days.toFloat() / totalScheduled30Days.toFloat()).coerceIn(0f, 1f)
        }

        val sortedByRate = stats.sortedWith(
            compareByDescending<HabitStats> { it.completionRate }
                .thenByDescending { it.currentStreak }
                .thenByDescending { it.totalCompletions }
        )
        val mostConsistent = sortedByRate.take(5)
        val leastConsistent = stats
            .sortedWith(compareBy<HabitStats> { it.completionRate }.thenBy { it.currentStreak })
            .take(5)

        val categoryBreakdown = HabitCategory.entries.mapNotNull { category ->
            val catStats = stats.filter { it.habit.category == category }
            if (catStats.isEmpty()) null
            else CategoryProgress(
                category = category,
                completedCount = catStats.sumOf { it.totalCompletions },
                scheduledCount = catStats.sumOf { it.scheduledDaysCount }
            )
        }

        return AnalyticsSummary(
            totalCompletedAllTime = totalCompletedAllTime,
            overallCompletionRate = overallRate,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            weeklyProgress = weeklyProgress,
            monthlyProgress = monthlyProgress,
            mostConsistentHabits = mostConsistent,
            leastConsistentHabits = leastConsistent,
            categoryBreakdown = categoryBreakdown
        )
    }

    private fun maxOfDate(a: AppDate, b: AppDate): AppDate = if (a >= b) a else b
}
