package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.NotificationPreferences
import com.example.model.ThemeMode
import com.example.model.UserPreferences
import com.example.model.UserProfile
import com.example.model.WeekStartDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lifeflow_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val PROFILE_IMAGE_URI = stringPreferencesKey("profile_image_uri")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val MORNING_REMINDER_ENABLED = booleanPreferencesKey("morning_reminder_enabled")
        val MORNING_REMINDER_TIME = stringPreferencesKey("morning_reminder_time")
        val EVENING_REMINDER_ENABLED = booleanPreferencesKey("evening_reminder_enabled")
        val EVENING_REMINDER_TIME = stringPreferencesKey("evening_reminder_time")
        val WEEK_START_DAY = stringPreferencesKey("week_start_day")
        val HAS_SEEDED_STARTERS = booleanPreferencesKey("has_seeded_starters")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val theme = ThemeMode.entries.firstOrNull { it.name == prefs[Keys.THEME_MODE] } ?: ThemeMode.SYSTEM
        val weekStart = WeekStartDay.entries.firstOrNull { it.name == prefs[Keys.WEEK_START_DAY] }
            ?: WeekStartDay.MONDAY
        UserPreferences(
            profile = UserProfile(
                name = prefs[Keys.USER_NAME] ?: "Alex Morgan",
                email = prefs[Keys.USER_EMAIL] ?: "alex@lifeflow.local",
                profileImageUri = prefs[Keys.PROFILE_IMAGE_URI]
            ),
            themeMode = theme,
            notifications = NotificationPreferences(
                remindersEnabled = prefs[Keys.REMINDERS_ENABLED] ?: true,
                morningReminderEnabled = prefs[Keys.MORNING_REMINDER_ENABLED] ?: true,
                morningReminderTime = prefs[Keys.MORNING_REMINDER_TIME] ?: "07:30",
                eveningReminderEnabled = prefs[Keys.EVENING_REMINDER_ENABLED] ?: true,
                eveningReminderTime = prefs[Keys.EVENING_REMINDER_TIME] ?: "21:00"
            ),
            weekStartDay = weekStart,
            hasSeededStarterRoutines = prefs[Keys.HAS_SEEDED_STARTERS] ?: false
        )
    }

    suspend fun updateProfile(name: String, email: String, profileImageUri: String?) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_NAME] = name.trim().ifBlank { "Alex Morgan" }
            prefs[Keys.USER_EMAIL] = email.trim().ifBlank { "alex@lifeflow.local" }
            if (profileImageUri.isNullOrBlank()) {
                prefs.remove(Keys.PROFILE_IMAGE_URI)
            } else {
                prefs[Keys.PROFILE_IMAGE_URI] = profileImageUri
            }
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs -> prefs[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setWeekStartDay(day: WeekStartDay) {
        context.dataStore.edit { prefs -> prefs[Keys.WEEK_START_DAY] = day.name }
    }

    suspend fun updateNotificationPreferences(notifications: NotificationPreferences) {
        context.dataStore.edit { prefs ->
            prefs[Keys.REMINDERS_ENABLED] = notifications.remindersEnabled
            prefs[Keys.MORNING_REMINDER_ENABLED] = notifications.morningReminderEnabled
            prefs[Keys.MORNING_REMINDER_TIME] = notifications.morningReminderTime
            prefs[Keys.EVENING_REMINDER_ENABLED] = notifications.eveningReminderEnabled
            prefs[Keys.EVENING_REMINDER_TIME] = notifications.eveningReminderTime
        }
    }

    suspend fun markStarterRoutinesSeeded() {
        context.dataStore.edit { prefs -> prefs[Keys.HAS_SEEDED_STARTERS] = true }
    }

    suspend fun resetProfileAndPreferences() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
