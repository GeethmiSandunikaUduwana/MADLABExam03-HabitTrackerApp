package com.example.pulsepath.fragments

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsepath.R
import com.example.pulsepath.adapters.MoodAdapter
import com.example.pulsepath.adapters.MoodCalendarAdapter
import com.example.pulsepath.databinding.FragmentMoodBinding
import com.example.pulsepath.models.MoodEntry
import com.example.pulsepath.utils.DataManager
import java.text.SimpleDateFormat
import java.util.*

class MoodFragment : Fragment() {

    private var _binding: FragmentMoodBinding? = null
    private val binding get() = _binding!!
    private lateinit var moodAdapter: MoodAdapter
    private lateinit var calendarAdapter: MoodCalendarAdapter
    private lateinit var dataManager: DataManager
    private val moodEntries = mutableListOf<MoodEntry>()
    private var isCalendarVisible = false
    private var currentCalendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataManager = DataManager(requireContext())
        setupEmojiSelector()
        setupRecyclerView()
        setupCalendar()
        setupCalendarToggle()
        setupMoodReminders()
        loadMoodEntries()
        setupSwipeToDelete()
    }

    private fun setupCalendar() {
        // Set up calendar RecyclerView
        binding.calendarRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        
        // Set up month navigation
        binding.prevMonthButton.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }
        
        binding.nextMonthButton.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }
        
        updateCalendar()
    }

    private fun updateCalendar() {
        val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentCalendar.time)
        binding.monthYearText.text = monthYear
        
        calendarAdapter = MoodCalendarAdapter(moodEntries) { selectedDate ->
            showDateMoodDialog(selectedDate)
        }
        binding.calendarRecyclerView.adapter = calendarAdapter
    }

    private fun showDateMoodDialog(selectedDate: Date) {
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dateString = dateFormatter.format(selectedDate)
        
        val dialog = AlertDialog.Builder(requireContext())
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
            note = "Logged from calendar",
            timestamp = date.time
        )

        // Save to DataManager
        dataManager.addMoodEntry(newEntry)
        
        // Update mood entries list
        moodEntries.clear()
        moodEntries.addAll(dataManager.loadMoodEntries())
        
        // Update adapters
        moodAdapter.notifyDataSetChanged()
        updateCalendar()
        updateEmptyState()
        
        android.widget.Toast.makeText(requireContext(), "Mood logged for $mood! 😊", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun setupCalendarToggle() {
        binding.calendarToggleButton.setOnClickListener {
            isCalendarVisible = !isCalendarVisible
            binding.calendarCard.visibility = if (isCalendarVisible) View.VISIBLE else View.GONE
        }
    }

    private fun setupMoodReminders() {
        // Load reminder settings
        val isEnabled = dataManager.getMoodReminderEnabled()
        val reminderTime = dataManager.getMoodReminderTime()
        
        binding.moodReminderSwitch.isChecked = isEnabled
        binding.moodReminderTimeText.text = formatTime(reminderTime)

        // Set up switch listener
        binding.moodReminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            dataManager.saveMoodReminderEnabled(isChecked)
            if (isChecked) {
                scheduleMoodReminder()
            } else {
                cancelMoodReminder()
            }
        }

        // Set up time picker
        binding.moodReminderTimeText.setOnClickListener {
            showTimePicker()
        }
    }

    private fun showTimePicker() {
        val currentTime = dataManager.getMoodReminderTime()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                newCalendar.set(Calendar.MINUTE, minute)
                newCalendar.set(Calendar.SECOND, 0)
                
                dataManager.saveMoodReminderTime(newCalendar.timeInMillis)
                binding.moodReminderTimeText.text = formatTime(newCalendar.timeInMillis)
                
                if (binding.moodReminderSwitch.isChecked) {
                    scheduleMoodReminder()
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun formatTime(timeInMillis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        return formatter.format(calendar.time)
    }

    private fun scheduleMoodReminder() {
        // Implement mood reminder scheduling
        // This would use AlarmManager similar to hydration reminders
    }

    private fun cancelMoodReminder() {
        // Implement mood reminder cancellation
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val moodEntry = moodEntries[position]
                showDeleteMoodDialog(moodEntry)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.moodHistoryRecyclerView)
    }

    private fun showDeleteMoodDialog(moodEntry: MoodEntry) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_delete))
            .setMessage(getString(R.string.delete_mood_message))
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                deleteMoodEntry(moodEntry)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                moodAdapter.notifyDataSetChanged() // Refresh to undo swipe
                dialog.dismiss()
            }
            .setOnCancelListener {
                moodAdapter.notifyDataSetChanged() // Refresh to undo swipe
            }
            .show()
    }

    private fun deleteMoodEntry(moodEntry: MoodEntry) {
        moodEntries.removeAll { it.id == moodEntry.id }
        dataManager.saveMoodEntries(moodEntries)
        moodAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun setupEmojiSelector() {
        binding.emojiHappy.setOnClickListener { logMood("😊", "Happy") }
        binding.emojiNeutral.setOnClickListener { logMood("😐", "Neutral") }
        binding.emojiSad.setOnClickListener { logMood("😢", "Sad") }
        binding.emojiAngry.setOnClickListener { logMood("😠", "Angry") }
        binding.emojiExcited.setOnClickListener { logMood("😄", "Excited") }
    }

    private fun logMood(emoji: String, mood: String) {
        val newEntry = MoodEntry(
            id = UUID.randomUUID().toString(),
            emoji = emoji,
            mood = mood,
            note = binding.moodNoteEditText.text.toString(),
            timestamp = System.currentTimeMillis()
        )

        // Save to DataManager
        dataManager.addMoodEntry(newEntry)

        moodEntries.add(0, newEntry)
        moodAdapter.notifyItemInserted(0)
        binding.moodNoteEditText.text?.clear()

        // Scroll to top
        binding.moodHistoryRecyclerView.smoothScrollToPosition(0)
        updateEmptyState()
    }

    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter(moodEntries,
            onDeleteMood = { moodEntry ->
                showDeleteMoodDialog(moodEntry)
            }
        )
        binding.moodHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = moodAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadMoodEntries() {
        moodEntries.clear()
        moodEntries.addAll(dataManager.loadMoodEntries())
        moodAdapter.notifyDataSetChanged()
        updateCalendar()
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (moodEntries.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.moodHistoryRecyclerView.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.moodHistoryRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when fragment becomes visible
        loadMoodEntries()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): MoodFragment {
            return MoodFragment()
        }
    }
}