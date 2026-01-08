package com.example.pulsepath.fragments

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pulsepath.MainActivity
import com.example.pulsepath.R
import com.example.pulsepath.adapters.DashboardMoodCalendarAdapter
import com.example.pulsepath.databinding.FragmentDashboardBinding
import com.example.pulsepath.models.MoodEntry
import com.example.pulsepath.utils.DataManager
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataManager: DataManager
    private lateinit var dashboardMoodCalendarAdapter: DashboardMoodCalendarAdapter
    private val moodEntries = mutableListOf<MoodEntry>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataManager = DataManager(requireContext())
        setupUserProfile()
        setupProgressBars()
        setupQuickActions()
        setupMoodCalendar()
        loadUserData()
    }

    private fun setupUserProfile() {
        // Set up user greeting based on time of day
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> getString(com.example.pulsepath.R.string.good_morning)
            in 12..17 -> getString(com.example.pulsepath.R.string.good_afternoon)
            else -> getString(com.example.pulsepath.R.string.good_evening)
        }
        binding.userGreeting.text = greeting

        // Set up edit profile button
        binding.editProfileButton.setOnClickListener {
            showEditProfileDialog()
        }
    }

    private fun setupProgressBars() {
        // Progress bars will be updated with real data in loadUserData()
    }

    private fun setupQuickActions() {
        binding.quickWater.setOnClickListener {
            // Add water intake
            incrementWaterIntake()
        }

        binding.quickMood.setOnClickListener {
            // Log mood - navigate to mood fragment
            (activity as? MainActivity)?.navigateToMood()
        }

        binding.quickHabit.setOnClickListener {
            // Add habit - navigate to habits fragment
            (activity as? MainActivity)?.navigateToHabits()
        }
    }

    private fun setupMoodCalendar() {
        // Set up mood calendar RecyclerView
        binding.moodCalendarRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
            requireContext(), 
            androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, 
            false
        )
        
        // Initialize calendar adapter
        dashboardMoodCalendarAdapter = DashboardMoodCalendarAdapter(moodEntries) { selectedDate ->
            showDashboardMoodDialog(selectedDate)
        }
        binding.moodCalendarRecyclerView.adapter = dashboardMoodCalendarAdapter
        
        // Set up mood calendar view all button
        binding.viewAllMoodButton.setOnClickListener {
            (activity as? MainActivity)?.navigateToMood()
        }
    }

    private fun showDashboardMoodDialog(selectedDate: Date) {
        val dateFormatter = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dateString = dateFormatter.format(selectedDate)
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Log Mood for $dateString")
            .setMessage("Select your mood for this date:")
            .setPositiveButton("😊 Happy") { _, _ ->
                logMoodForDate("😊", "Happy", selectedDate)
            }
            .setNeutralButton("😐 Neutral") { _, _ ->
                logMoodForDate("😐", "Neutral", selectedDate)
            }
            .setNegativeButton("😢 Sad") { _, _ ->
                logMoodForDate("😢", "Sad", selectedDate)
            }
            .create()
        
        dialog.show()
    }

    private fun logMoodForDate(emoji: String, mood: String, date: Date) {
        val newEntry = MoodEntry(
            id = UUID.randomUUID().toString(),
            emoji = emoji,
            mood = mood,
            note = "Logged from dashboard calendar",
            timestamp = date.time
        )

        // Save to DataManager
        dataManager.addMoodEntry(newEntry)
        
        // Update mood entries list
        moodEntries.clear()
        moodEntries.addAll(dataManager.loadMoodEntries())
        
        // Update calendar adapter
        dashboardMoodCalendarAdapter.notifyDataSetChanged()
        
        // Update mood summary
        updateMoodSummary()
        
        android.widget.Toast.makeText(requireContext(), "Mood logged for $mood! 😊", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun showEditProfileDialog() {
        val currentName = dataManager.getUserName()
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit_profile))
            .setView(R.layout.dialog_edit_profile)
            .setPositiveButton("Save") { dialogInterface: android.content.DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface: android.content.DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
        
        // Handle the dialog content
        val nameEditText = dialog.findViewById<android.widget.EditText>(R.id.editTextName)
        nameEditText?.setText(currentName)
        
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            val newName = nameEditText?.text?.toString()?.trim()
            if (!newName.isNullOrEmpty()) {
                dataManager.saveUserName(newName)
                binding.userName.text = newName
                dialog.dismiss()
            }
        }
    }

    private fun loadUserData() {
        // Load user name
        binding.userName.text = dataManager.getUserName()
        
        // Calculate streak (simplified - you can implement proper streak logic)
        val streak = dataManager.getStreakDays()
        binding.userStreak.text = "🔥 $streak ${getString(com.example.pulsepath.R.string.day_streak)}"
        
        // Load progress data
        val habitProgress = dataManager.getHabitProgress()
        binding.habitProgressBar.progress = habitProgress
        binding.habitProgressText.text = "$habitProgress%"

        val waterIntake = dataManager.getWaterIntake()
        val waterTarget = dataManager.getWaterTarget()
        val waterProgress = if (waterTarget > 0) (waterIntake * 100) / waterTarget else 0
        binding.waterProgressBar.progress = waterProgress
        binding.waterCountText.text = "$waterIntake/$waterTarget glasses"

        // Load mood entries for calendar
        moodEntries.clear()
        moodEntries.addAll(dataManager.loadMoodEntries())
        
        // Update calendar if adapter exists
        if (::dashboardMoodCalendarAdapter.isInitialized) {
            dashboardMoodCalendarAdapter.notifyDataSetChanged()
        }

        // Load mood data
        updateMoodSummary()
    }

    private fun updateMoodSummary() {
        val lastMood = dataManager.getLastMood()
        binding.moodSummaryText.text = lastMood
        
        // Extract emoji from mood string
        val emoji = if (lastMood.contains("😊")) "😊"
        else if (lastMood.contains("😐")) "😐"
        else if (lastMood.contains("😢")) "😢"
        else if (lastMood.contains("😠")) "😠"
        else if (lastMood.contains("😄")) "😄"
        else "😊"
        binding.moodEmoji.text = emoji
    }

    private fun incrementWaterIntake() {
        val current = dataManager.getWaterIntake()
        val newValue = current + 1
        dataManager.saveWaterIntake(newValue)
        
        // Update UI
        val waterTarget = dataManager.getWaterTarget()
        val waterProgress = if (waterTarget > 0) (newValue * 100) / waterTarget else 0
        binding.waterProgressBar.progress = waterProgress
        binding.waterCountText.text = "$newValue/$waterTarget glasses"
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when fragment becomes visible
        loadUserData()
        
        // Update calendar if adapter exists
        if (::dashboardMoodCalendarAdapter.isInitialized) {
            moodEntries.clear()
            moodEntries.addAll(dataManager.loadMoodEntries())
            dashboardMoodCalendarAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): DashboardFragment {
            return DashboardFragment()
        }
    }
}