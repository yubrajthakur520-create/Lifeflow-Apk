package com.example.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.model.Habit
import com.example.model.NotificationPreferences
import java.util.Calendar

object HabitReminderScheduler {

    const val CHANNEL_ID = "lifeflow_reminders"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_MESSAGE = "extra_message"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val EXTRA_HOUR = "extra_hour"
    const val EXTRA_MINUTE = "extra_minute"
    const val EXTRA_REQUEST_CODE = "extra_request_code"
    const val EXTRA_DAYS_MASK = "extra_days_mask"

    private const val MORNING_REQ_CODE = 1001
    private const val EVENING_REQ_CODE = 1002

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun syncAllReminders(
        context: Context,
        habits: List<Habit>,
        prefs: NotificationPreferences
    ) {
        ensureChannel(context)

        cancelAlarm(context, MORNING_REQ_CODE)
        cancelAlarm(context, EVENING_REQ_CODE)

        if (!prefs.remindersEnabled) {
            habits.forEach { cancelHabitReminder(context, it.id) }
            return
        }

        if (prefs.morningReminderEnabled) {
            parseHourMinute(prefs.morningReminderTime)?.let { (h, m) ->
                scheduleDailyAlarm(
                    context = context,
                    requestCode = MORNING_REQ_CODE,
                    hour = h,
                    minute = m,
                    title = "Good Morning! Ready for Lifeflow",
                    message = "Check off your Morning Routine and build momentum for today.",
                    daysMask = "1,2,3,4,5,6,7"
                )
            }
        }

        if (prefs.eveningReminderEnabled) {
            parseHourMinute(prefs.eveningReminderTime)?.let { (h, m) ->
                scheduleDailyAlarm(
                    context = context,
                    requestCode = EVENING_REQ_CODE,
                    hour = h,
                    minute = m,
                    title = "Evening Progress Review",
                    message = "Take a moment to log remaining habits and prepare tomorrow's schedule.",
                    daysMask = "1,2,3,4,5,6,7"
                )
            }
        }

        habits.forEach { habit ->
            val reqCode = habitRequestCode(habit.id)
            if (habit.isActive && habit.reminderEnabled && !habit.reminderTime.isNullOrBlank()) {
                val parsed = parseHourMinute(habit.reminderTime)
                if (parsed != null) {
                    scheduleDailyAlarm(
                        context = context,
                        requestCode = reqCode,
                        hour = parsed.first,
                        minute = parsed.second,
                        title = "Habit Reminder: ${habit.title}",
                        message = habit.description.ifBlank {
                            "Time for your ${habit.category.displayName} habit."
                        },
                        daysMask = habit.scheduledDays.sorted().joinToString(",")
                    )
                }
            } else {
                cancelAlarm(context, reqCode)
            }
        }
    }

    fun cancelHabitReminder(context: Context, habitId: String) {
        cancelAlarm(context, habitRequestCode(habitId))
    }

    fun showImmediateNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ): Boolean {
        ensureChannel(context)
        if (!hasNotificationPermission(context)) return false

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    internal fun scheduleDailyAlarm(
        context: Context,
        requestCode: Int,
        hour: Int,
        minute: Int,
        title: String,
        message: String,
        daysMask: String,
        forceNextDay: Boolean = false
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_MESSAGE, message)
            putExtra(EXTRA_NOTIFICATION_ID, requestCode)
            putExtra(EXTRA_HOUR, hour)
            putExtra(EXTRA_MINUTE, minute)
            putExtra(EXTRA_REQUEST_CODE, requestCode)
            putExtra(EXTRA_DAYS_MASK, daysMask)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (forceNextDay || timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerCal.timeInMillis,
            pendingIntent
        )
    }

    private fun cancelAlarm(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HabitReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun habitRequestCode(habitId: String): Int = 2000 + (habitId.hashCode() and 0x7FFF)

    private fun parseHourMinute(time24: String): Pair<Int, Int>? {
        val parts = time24.trim().split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h to m
    }
}
