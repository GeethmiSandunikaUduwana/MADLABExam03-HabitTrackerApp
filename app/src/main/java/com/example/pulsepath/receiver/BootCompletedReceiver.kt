package com.example.pulsepath.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Restore hydration reminders if they were enabled
            val sharedPrefs = context.getSharedPreferences("pulsepath_data", Context.MODE_PRIVATE)
            val isReminderEnabled = sharedPrefs.getBoolean("reminder_enabled", false)

            if (isReminderEnabled) {
                val interval = sharedPrefs.getInt("reminder_interval", 2)
                scheduleReminders(context, interval)
            }
        }
    }

    private fun scheduleReminders(context: Context, intervalHours: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(context, HydrationReminderReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        // Set first reminder to now + interval
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(java.util.Calendar.HOUR_OF_DAY, intervalHours)
        }

        alarmManager.setRepeating(
            android.app.AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            (intervalHours * 60 * 60 * 1000).toLong(),
            pendingIntent
        )
    }
}