package com.example.pulsepath.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.pulsepath.R
import com.example.pulsepath.databinding.FragmentHydrationBinding
import com.example.pulsepath.receiver.HydrationReminderReceiver
import com.example.pulsepath.utils.DataManager
import java.util.Calendar

class HydrationFragment : Fragment() {

    private var _binding: FragmentHydrationBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataManager: DataManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHydrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataManager = DataManager(requireContext())
        loadSettings()
        setupClickListeners()
        setupWaterIntakeTracker()

        // Test button for notifications (remove in production)
        binding.testNotificationButton.setOnClickListener {
            showTestNotification()
        }
    }

    private fun showTestNotification() {
        val notificationHelper = com.example.pulsepath.utils.NotificationHelper
        notificationHelper.showHydrationReminder(requireContext())
        Toast.makeText(requireContext(), "Test notification sent", Toast.LENGTH_SHORT).show()
    }

    private fun loadSettings() {
        val isReminderEnabled = dataManager.getReminderEnabled()
        val interval = dataManager.getReminderInterval()

        binding.reminderSwitch.isChecked = isReminderEnabled
        binding.intervalSeekBar.progress = interval
        binding.intervalValueText.text = "$interval hours"
    }

    private fun setupClickListeners() {
        binding.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveSettings()
            if (isChecked) {
                if (scheduleReminders()) {
                    Toast.makeText(requireContext(), "Reminders enabled", Toast.LENGTH_SHORT).show()
                }
            } else {
                cancelReminders()
                Toast.makeText(requireContext(), "Reminders disabled", Toast.LENGTH_SHORT).show()
            }
        }

        binding.intervalSeekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.intervalValueText.text = "$progress hours"
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                saveSettings()
                if (binding.reminderSwitch.isChecked) {
                    cancelReminders()
                    scheduleReminders()
                }
            }
        })

        binding.addGlassButton.setOnClickListener {
            incrementWaterIntake()
        }

        binding.resetWaterButton.setOnClickListener {
            resetWaterIntake()
        }
    }

    private fun setupWaterIntakeTracker() {
        val glasses = dataManager.getWaterIntake()
        updateWaterIntakeUI(glasses)
    }

    private fun incrementWaterIntake() {
        val current = dataManager.getWaterIntake()
        val newValue = current + 1
        dataManager.saveWaterIntake(newValue)
        updateWaterIntakeUI(newValue)
        Toast.makeText(requireContext(), "Water intake recorded! 💧", Toast.LENGTH_SHORT).show()
    }

    private fun resetWaterIntake() {
        dataManager.saveWaterIntake(0)
        updateWaterIntakeUI(0)
        Toast.makeText(requireContext(), "Water intake reset", Toast.LENGTH_SHORT).show()
    }

    private fun updateWaterIntakeUI(glasses: Int) {
        val target = dataManager.getWaterTarget()
        val progress = if (target > 0) (glasses * 100) / target else 0
        binding.waterProgressBar.progress = progress
        binding.waterCountText.text = "$glasses/$target glasses"
    }

    private fun saveSettings() {
        dataManager.saveReminderEnabled(binding.reminderSwitch.isChecked)
        dataManager.saveReminderInterval(binding.intervalSeekBar.progress)
    }

    private fun scheduleReminders(): Boolean {
        val intervalHours = dataManager.getReminderInterval()
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(requireContext(), HydrationReminderReceiver::class.java)

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            pendingIntentFlags
        )

        // Set first reminder to current time + interval
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.HOUR, intervalHours)
        }

        return try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                        // Schedule repeating reminders
                        scheduleRepeatingReminders(intervalHours)
                        true
                    } else {
                        // Fallback to inexact alarm
                        alarmManager.setInexactRepeating(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            AlarmManager.INTERVAL_HOUR * intervalHours,
                            pendingIntent
                        )
                        true
                    }
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    // Schedule repeating reminders
                    scheduleRepeatingReminders(intervalHours)
                    true
                }
                else -> {
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        (intervalHours * 60 * 60 * 1000).toLong(),
                        pendingIntent
                    )
                    true
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Cannot set reminders: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun scheduleRepeatingReminders(intervalHours: Int) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), HydrationReminderReceiver::class.java)

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            1, // Different request code for repeating alarms
            intent,
            pendingIntentFlags
        )

        val intervalMillis = (intervalHours * 60 * 60 * 1000).toLong()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + intervalMillis,
                intervalMillis,
                pendingIntent
            )
        } else {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + intervalMillis,
                intervalMillis,
                pendingIntent
            )
        }
    }

    private fun cancelReminders() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), HydrationReminderReceiver::class.java)

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            pendingIntentFlags
        )

        try {
            alarmManager.cancel(pendingIntent)
            
            // Cancel repeating alarms with different request code
            val repeatingPendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                1,
                intent,
                pendingIntentFlags
            )
            alarmManager.cancel(repeatingPendingIntent)
        } catch (e: Exception) {
            // Handle any exceptions
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when fragment becomes visible
        setupWaterIntakeTracker()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): HydrationFragment {
            return HydrationFragment()
        }
    }
}