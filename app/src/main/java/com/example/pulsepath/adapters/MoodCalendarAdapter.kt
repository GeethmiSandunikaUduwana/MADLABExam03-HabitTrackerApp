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

class MoodCalendarAdapter(
    private val moodEntries: List<MoodEntry>,
    private val onDateClick: (Date) -> Unit
) : RecyclerView.Adapter<MoodCalendarAdapter.CalendarViewHolder>() {

    private val calendar = Calendar.getInstance()
    private val currentMonth = calendar.get(Calendar.MONTH)
    private val currentYear = calendar.get(Calendar.YEAR)
    
    // Get first day of current month
    private val firstDayOfMonth = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYear)
        set(Calendar.MONTH, currentMonth)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    
    // Get last day of current month
    private val lastDayOfMonth = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYear)
        set(Calendar.MONTH, currentMonth)
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val dayNumber = position + 1
        val dayDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, dayNumber)
        }
        
        holder.bind(dayNumber, dayDate.time, moodEntries)
    }

    override fun getItemCount(): Int {
        return lastDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    inner class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayNumberText: TextView = itemView.findViewById(R.id.dayNumberText)
        private val moodEmojiText: TextView = itemView.findViewById(R.id.moodEmojiText)

        fun bind(dayNumber: Int, date: Date, moodEntries: List<MoodEntry>) {
            dayNumberText.text = dayNumber.toString()
            
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
                today.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH)) {
                dayNumberText.setTextColor(itemView.context.getColor(R.color.primary_color))
                dayNumberText.textSize = 16f
            } else {
                dayNumberText.setTextColor(itemView.context.getColor(R.color.text_primary))
                dayNumberText.textSize = 14f
            }
            
            itemView.setOnClickListener {
                onDateClick(date)
            }
        }
    }
}


