package com.example.pulsepath.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsepath.R
import com.example.pulsepath.models.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

class DashboardMoodCalendarAdapter(
    private val moodEntries: List<MoodEntry>,
    private val onDateClick: (Date) -> Unit
) : RecyclerView.Adapter<DashboardMoodCalendarAdapter.DashboardCalendarViewHolder>() {

    // Show last 7 days
    private val calendar = Calendar.getInstance()
    private val recentDates = mutableListOf<Date>()

    init {
        generateRecentDates()
    }

    private fun generateRecentDates() {
        recentDates.clear()
        val today = Calendar.getInstance()
        
        // Generate last 7 days
        for (i in 6 downTo 0) {
            val date = Calendar.getInstance().apply {
                timeInMillis = today.timeInMillis
                add(Calendar.DAY_OF_YEAR, -i)
            }
            recentDates.add(date.time)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardCalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_calendar_day, parent, false)
        return DashboardCalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: DashboardCalendarViewHolder, position: Int) {
        val date = recentDates[position]
        holder.bind(date, moodEntries)
    }

    override fun getItemCount(): Int = recentDates.size

    inner class DashboardCalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayText: TextView = itemView.findViewById(R.id.dayText)
        private val moodEmojiText: TextView = itemView.findViewById(R.id.moodEmojiText)

        fun bind(date: Date, moodEntries: List<MoodEntry>) {
            val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())
            val dayName = dayFormatter.format(date)
            
            val dayNumberFormatter = SimpleDateFormat("d", Locale.getDefault())
            val dayNumber = dayNumberFormatter.format(date)
            
            dayText.text = dayName
            
            // Check if there's a mood entry for this date
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateString = dateFormatter.format(date)
            
            val moodForDate = moodEntries.find { entry ->
                val entryDate = dateFormatter.format(Date(entry.timestamp))
                entryDate == dateString
            }
            
            if (moodForDate != null) {
                moodEmojiText.text = moodForDate.emoji
                moodEmojiText.visibility = View.VISIBLE
                itemView.setBackgroundResource(R.drawable.calendar_day_with_mood)
            } else {
                moodEmojiText.visibility = View.GONE
                itemView.setBackgroundResource(R.drawable.calendar_day_default)
            }
            
            // Make today's date more prominent
            val today = Calendar.getInstance()
            val currentDate = Calendar.getInstance().apply { time = date }
            
            if (today.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == currentDate.get(Calendar.DAY_OF_YEAR)) {
                dayText.setTextColor(itemView.context.getColor(R.color.primary_color))
                dayText.textSize = 14f
            } else {
                dayText.setTextColor(itemView.context.getColor(R.color.text_primary))
                dayText.textSize = 12f
            }
            
            itemView.setOnClickListener {
                onDateClick(date)
            }
        }
    }
}


