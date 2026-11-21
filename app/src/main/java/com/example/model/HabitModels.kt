package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Shower
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.CatFitness
import com.example.ui.theme.CatHealth
import com.example.ui.theme.CatLifestyle
import com.example.ui.theme.CatMindfulness
import com.example.ui.theme.CatPersonalCare
import com.example.ui.theme.CatProductivity
import com.example.ui.theme.CatStudy
import java.util.Calendar
import java.util.Locale

/**
 * API-24 safe immutable calendar date (YYYY-MM-DD).
 * dayOfWeek uses ISO-8601 convention: 1 = Monday .. 7 = Sunday.
 */
data class AppDate(
    val year: Int,
    val month: Int, // 1..12
    val day: Int    // 1..31
) : Comparable<AppDate> {

    fun toIsoString(): String = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)

    fun toCalendar(): Calendar = Calendar.getInstance().apply {
        clear()
        set(year, month - 1, day, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    /** ISO day of week: 1 = Monday .. 7 = Sunday */
    val isoDayOfWeek: Int
        get() {
            val calDay = toCalendar().get(Calendar.DAY_OF_WEEK)
            return when (calDay) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }
        }

    fun plusDays(days: Int): AppDate {
        val cal = toCalendar()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return fromCalendar(cal)
    }

    fun minusDays(days: Int): AppDate = plusDays(-days)

    fun plusMonths(months: Int): AppDate {
        val cal = Calendar.getInstance().apply {
            clear()
            set(year, month - 1, 1, 0, 0, 0)
        }
        cal.add(Calendar.MONTH, months)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val targetDay = day.coerceAtMost(maxDay)
        return AppDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, targetDay)
    }

    fun daysInMonth(): Int {
        val cal = Calendar.getInstance().apply {
            clear()
            set(year, month - 1, 1)
        }
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun firstDayOfMonth(): AppDate = AppDate(year, month, 1)

    fun formatReadable(): String {
        val monthNames = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return "${weekdayFullName()}, ${monthNames[month - 1]} $day, $year"
    }

    fun formatMonthYear(): String {
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return "${monthNames[month - 1]} $year"
    }

    fun formatShortMonthDay(): String {
        val monthNames = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return "${monthNames[month - 1]} $day"
    }

    fun weekdayFullName(): String = when (isoDayOfWeek) {
        1 -> "Monday"
        2 -> "Tuesday"
        3 -> "Wednesday"
        4 -> "Thursday"
        5 -> "Friday"
        6 -> "Saturday"
        else -> "Sunday"
    }

    fun weekdayShortName(): String = when (isoDayOfWeek) {
        1 -> "Mon"
        2 -> "Tue"
        3 -> "Wed"
        4 -> "Thu"
        5 -> "Fri"
        6 -> "Sat"
        else -> "Sun"
    }

    override fun compareTo(other: AppDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    companion object {
        fun today(): AppDate = fromCalendar(Calendar.getInstance())

        fun fromCalendar(cal: Calendar): AppDate = AppDate(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + 1,
            day = cal.get(Calendar.DAY_OF_MONTH)
        )

        fun parseIso(iso: String): AppDate? {
            val parts = iso.trim().split("-")
            if (parts.size != 3) return null
            val y = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            val d = parts[2].toIntOrNull() ?: return null
            if (m !in 1..12 || d !in 1..31) return null
            return AppDate(y, m, d)
        }
    }
}

enum class HabitCategory(val displayName: String, val accentColor: Color) {
    FITNESS("Fitness", CatFitness),
    HEALTH("Health", CatHealth),
    STUDY("Study", CatStudy),
    PRODUCTIVITY("Productivity", CatProductivity),
    PERSONAL_CARE("Personal Care", CatPersonalCare),
    MINDFULNESS("Mindfulness", CatMindfulness),
    LIFESTYLE("Lifestyle", CatLifestyle);

    companion object {
        fun fromKey(key: String): HabitCategory =
            entries.firstOrNull { it.name == key || it.displayName.equals(key, ignoreCase = true) }
                ?: LIFESTYLE
    }
}

enum class RoutinePeriod(val displayName: String, val timeHint: String) {
    MORNING("Morning Routine", "06:00 – 11:59"),
    AFTERNOON("Afternoon Routine", "12:00 – 16:59"),
    EVENING("Evening Routine", "17:00 – 20:59"),
    NIGHT("Night Routine", "21:00 – 23:59"),
    ANYTIME("Anytime", "Flexible");

    companion object {
        fun fromKey(key: String): RoutinePeriod =
            entries.firstOrNull { it.name == key || it.displayName.equals(key, ignoreCase = true) }
                ?: ANYTIME
    }
}

enum class HabitFrequency(val displayName: String) {
    DAILY("Every Day"),
    WEEKDAYS("Weekdays"),
    CUSTOM("Custom Days");

    companion object {
        fun fromKey(key: String): HabitFrequency =
            entries.firstOrNull { it.name == key } ?: DAILY
    }
}

enum class HabitIcon(val key: String, val label: String, val vector: ImageVector) {
    WB_SUNNY("wb_sunny", "Morning", Icons.Rounded.WbSunny),
    WATER_DROP("water_drop", "Hydrate", Icons.Rounded.WaterDrop),
    FITNESS_CENTER("fitness_center", "Workout", Icons.Rounded.FitnessCenter),
    DIRECTIONS_RUN("directions_run", "Run", Icons.AutoMirrored.Rounded.DirectionsRun),
    DIRECTIONS_WALK("directions_walk", "Walk", Icons.AutoMirrored.Rounded.DirectionsWalk),
    SELF_IMPROVEMENT("self_improvement", "Mindful", Icons.Rounded.SelfImprovement),
    SHOWER("shower", "Shower", Icons.Rounded.Shower),
    RESTAURANT("restaurant", "Meal", Icons.Rounded.Restaurant),
    MENU_BOOK("menu_book", "Study", Icons.AutoMirrored.Rounded.MenuBook),
    CHECKLIST("checklist", "Tasks", Icons.Rounded.Checklist),
    EDIT_NOTE("edit_note", "Plan", Icons.Rounded.EditNote),
    SPA("spa", "Skincare", Icons.Rounded.Spa),
    PSYCHOLOGY("psychology", "Focus", Icons.Rounded.Psychology),
    FAVORITE("favorite", "Health", Icons.Rounded.Favorite),
    BEDTIME("bedtime", "Sleep", Icons.Rounded.Bedtime),
    NIGHTS_STAY("nights_stay", "Night", Icons.Rounded.NightsStay),
    ALARM("alarm", "Wake", Icons.Rounded.Alarm),
    CHECK_CIRCLE("check_circle", "Goal", Icons.Rounded.CheckCircle);

    companion object {
        fun fromKey(key: String): HabitIcon =
            entries.firstOrNull { it.key == key } ?: CHECK_CIRCLE
    }
}

enum class ThemeMode(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class WeekStartDay(val displayName: String, val isoDay: Int) {
    MONDAY("Monday", 1),
    SUNDAY("Sunday", 7)
}

data class UserProfile(
    val name: String = "Alex Morgan",
    val email: String = "alex@lifeflow.local",
    val profileImageUri: String? = null
)

data class NotificationPreferences(
    val remindersEnabled: Boolean = true,
    val morningReminderEnabled: Boolean = true,
    val morningReminderTime: String = "07:30",
    val eveningReminderEnabled: Boolean = true,
    val eveningReminderTime: String = "21:00"
)

data class UserPreferences(
    val profile: UserProfile = UserProfile(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notifications: NotificationPreferences = NotificationPreferences(),
    val weekStartDay: WeekStartDay = WeekStartDay.MONDAY,
    val hasSeededStarterRoutines: Boolean = false
)

data class Habit(
    val id: String,
    val title: String,
    val description: String,
    val category: HabitCategory,
    val routinePeriod: RoutinePeriod,
    val icon: HabitIcon,
    val frequency: HabitFrequency,
    val scheduledDays: Set<Int>, // 1=Mon..7=Sun
    val reminderTime: String?,   // "HH:mm"
    val reminderEnabled: Boolean,
    val startDate: AppDate,
    val targetDurationMinutes: Int?,
    val isActive: Boolean,
    val isStarterRoutine: Boolean,
    val sortOrder: Int,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

data class HabitCompletion(
    val id: String,
    val habitId: String,
    val date: AppDate,
    val completed: Boolean,
    val completedAtEpochMs: Long
)

data class HabitStats(
    val habit: Habit,
    val currentStreak: Int,
    val longestStreak: Int,
    val completionRate: Float, // 0f..1f
    val totalCompletions: Int,
    val scheduledDaysCount: Int
)

data class DailyProgress(
    val date: AppDate,
    val scheduledCount: Int,
    val completedCount: Int
) {
    val completionFraction: Float
        get() = if (scheduledCount == 0) 0f else completedCount.toFloat() / scheduledCount.toFloat()

    val completionPercent: Int
        get() = (completionFraction * 100f).toInt().coerceIn(0, 100)
}

data class DashboardSummary(
    val greeting: String,
    val today: AppDate,
    val totalScheduledToday: Int,
    val completedToday: Int,
    val remainingToday: Int,
    val completionPercentToday: Int,
    val currentOverallStreak: Int,
    val quote: MotivationalQuote
)

data class CategoryProgress(
    val category: HabitCategory,
    val completedCount: Int,
    val scheduledCount: Int
) {
    val rate: Float get() = if (scheduledCount == 0) 0f else completedCount.toFloat() / scheduledCount.toFloat()
}

data class AnalyticsSummary(
    val totalCompletedAllTime: Int,
    val overallCompletionRate: Float,
    val currentStreak: Int,
    val longestStreak: Int,
    val weeklyProgress: List<DailyProgress>,
    val monthlyProgress: List<DailyProgress>,
    val mostConsistentHabits: List<HabitStats>,
    val leastConsistentHabits: List<HabitStats>,
    val categoryBreakdown: List<CategoryProgress>
)

data class MotivationalQuote(
    val text: String,
    val author: String
)

object MotivationalQuotes {
    val quotes = listOf(
        MotivationalQuote("Small habits. Better days. Stronger you.", "Lifeflow"),
        MotivationalQuote("We are what we repeatedly do. Excellence, then, is not an act, but a habit.", "Will Durant"),
        MotivationalQuote("You do not rise to the level of your goals. You fall to the level of your systems.", "James Clear"),
        MotivationalQuote("Success is the sum of small efforts, repeated day in and day out.", "Robert Collier"),
        MotivationalQuote("Consistency is more important than perfection. Just show up today.", "Lifeflow"),
        MotivationalQuote("How we spend our days is, of course, how we spend our lives.", "Annie Dillard"),
        MotivationalQuote("A year from now you may wish you had started today.", "Karen Lamb"),
        MotivationalQuote("Motivation is what gets you started. Habit is what keeps you going.", "Jim Ryun")
    )

    fun quoteForDate(date: AppDate): MotivationalQuote {
        val index = ((date.year * 31 + date.month * 12 + date.day) % quotes.size).let {
            if (it < 0) it + quotes.size else it
        }
        return quotes[index]
    }
}

fun formatTime12Hour(time24: String?): String {
    if (time24.isNullOrBlank()) return ""
    val parts = time24.split(":")
    if (parts.size != 2) return time24
    val hour = parts[0].toIntOrNull() ?: return time24
    val minute = parts[1].toIntOrNull() ?: return time24
    val amPm = if (hour < 12) "AM" else "PM"
    val h12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(Locale.US, "%d:%02d %s", h12, minute, amPm)
}
