package com.example.pulsepath.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pulsepath.utils.NotificationHelper

class HydrationReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.showHydrationReminder(context)
    }
}