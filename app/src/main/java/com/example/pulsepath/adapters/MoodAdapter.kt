package com.example.pulsepath.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsepath.R
import com.example.pulsepath.models.MoodEntry
import com.example.pulsepath.utils.DateUtils

class MoodAdapter(
    private val entries: MutableList<MoodEntry>,
    private val onDeleteMood: (MoodEntry) -> Unit // Add delete callback
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val emojiTextView: TextView = itemView.findViewById(R.id.mood_emoji)
        private val dateTextView: TextView = itemView.findViewById(R.id.mood_date)
        private val noteTextView: TextView = itemView.findViewById(R.id.mood_note)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.mood_delete_button)
        private val shareButton: ImageButton = itemView.findViewById(R.id.mood_share)

        fun bind(entry: MoodEntry) {
            emojiTextView.text = entry.emoji
            dateTextView.text = DateUtils.formatDate(entry.timestamp)
            noteTextView.text = if (entry.note.isNotBlank()) entry.note else "No note"

            // Set up delete button
            deleteButton.setOnClickListener {
                onDeleteMood(entry)
            }

            // Set up share button
            shareButton.setOnClickListener {
                // Share functionality (optional)
                val shareMessage = "On ${DateUtils.formatDate(entry.timestamp)}, I was feeling ${entry.emoji} ${entry.mood}. ${if (entry.note.isNotBlank()) "Note: ${entry.note}" else ""}"
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, shareMessage)
                }
                itemView.context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Mood"))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_entry, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount(): Int = entries.size

    // Method to remove mood entry from adapter
    fun removeMoodEntry(entry: MoodEntry) {
        val position = entries.indexOfFirst { it.id == entry.id }
        if (position != -1) {
            entries.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}