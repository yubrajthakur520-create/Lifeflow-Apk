package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

class HabitReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(HabitReminderScheduler.EXTRA_TITLE) ?: "Lifeflow Habit Reminder"
        val message = intent.getStringExtra(HabitReminderScheduler.EXTRA_MESSAGE) ?: "Keep your streak alive today!"
        val notificationId = intent.getIntExtra(HabitReminderScheduler.EXTRA_NOTIFICATION_ID, 1000)
        val hour = intent.getIntExtra(HabitReminderScheduler.EXTRA_HOUR, 9)
        val minute = intent.getIntExtra(HabitReminderScheduler.EXTRA_MINUTE, 0)
        val requestCode = intent.getIntExtra(HabitReminderScheduler.EXTRA_REQUEST_CODE, notificationId)
        val daysMask = intent.getStringExtra(HabitReminderScheduler.EXTRA_DAYS_MASK) ?: "1,2,3,4,5,6,7"

        val todayIsoDay = currentIsoDayOfWeek()
        val allowedDays = daysMask.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        if (allowedDays.isEmpty() || todayIsoDay in allowedDays) {
            HabitReminderScheduler.showImmediateNotification(
                context = context,
                notificationId = notificationId,
                title = title,
                message = message
            )
        }

        // Schedule next day's reminder
        HabitReminderScheduler.scheduleDailyAlarm(
            context = context,
            requestCode = requestCode,
            hour = hour,
            minute = minute,
            title = title,
            message = message,
            daysMask = daysMask,
            forceNextDay = true
        )
    }

    private fun currentIsoDayOfWeek(): Int {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
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
}
