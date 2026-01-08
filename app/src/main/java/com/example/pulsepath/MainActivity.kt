package com.example.pulsepath

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.pulsepath.databinding.ActivityMainBinding
import com.example.pulsepath.fragments.DashboardFragment
import com.example.pulsepath.fragments.HabitsFragment
import com.example.pulsepath.fragments.HydrationFragment
import com.example.pulsepath.fragments.MoodFragment
import com.example.pulsepath.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar to ensure no black bar appears
        supportActionBar?.hide()

        // Set keyboard handling to adjustPan to prevent navigation bar moving up
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        // Check and request notification permission for Android 13+
        checkNotificationPermission()

        // Set up bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> replaceFragment(DashboardFragment())
                R.id.nav_habits -> replaceFragment(HabitsFragment())
                R.id.nav_mood -> replaceFragment(MoodFragment())
                R.id.nav_hydration -> replaceFragment(HydrationFragment())
                R.id.nav_settings -> replaceFragment(SettingsFragment())
            }
            true
        }

        // Set default fragment
        if (savedInstanceState == null) {
            replaceFragment(DashboardFragment())
            binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        // Also request exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(android.app.AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                android.widget.Toast.makeText(this, "Notification permission granted", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                android.widget.Toast.makeText(this, "Notification permission denied", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Navigation methods for quick actions
    fun navigateToHydration() {
        replaceFragment(HydrationFragment())
        binding.bottomNavigation.selectedItemId = R.id.nav_hydration
    }

    fun navigateToMood() {
        replaceFragment(MoodFragment())
        binding.bottomNavigation.selectedItemId = R.id.nav_mood
    }

    fun navigateToHabits() {
        replaceFragment(HabitsFragment())
        binding.bottomNavigation.selectedItemId = R.id.nav_habits
    }
}