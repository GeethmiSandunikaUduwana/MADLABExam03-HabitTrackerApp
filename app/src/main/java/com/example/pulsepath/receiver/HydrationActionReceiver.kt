package com.example.pulsepath.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.pulsepath.R
import com.example.pulsepath.utils.DataManager

class HydrationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "ADD_WATER_ACTION" -> {
                val dataManager = DataManager(context)
                val current = dataManager.getWaterIntake()
                val newValue = current + 1
                dataManager.saveWaterIntake(newValue)

                // Show confirmation
                Toast.makeText(
                    context,
                    context.getString(R.string.water_added_success),
                    Toast.LENGTH_SHORT
                ).show()

                // Cancel the notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.cancel(1)
            }
        }
    }
}