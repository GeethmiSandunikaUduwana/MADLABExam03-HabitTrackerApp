package com.example.pulsepath.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsepath.R
import com.example.pulsepath.models.Habit

class HabitAdapter(
    private val habits: MutableList<Habit>,
    private val onCheckChanged: (Habit, Boolean) -> Unit,
    private val onDeleteHabit: (Habit) -> Unit,
    private val onEditHabit: (Habit) -> Unit // Add edit callback
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.habit_name)
        private val categoryTextView: TextView = itemView.findViewById(R.id.habit_category)
        private val progressTextView: TextView = itemView.findViewById(R.id.habit_progress_text)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.habit_progress_bar)
        private val checkBox: CheckBox = itemView.findViewById(R.id.habit_checkbox)
        private val targetTextView: TextView = itemView.findViewById(R.id.habit_target)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.habit_delete_button)
        private val editButton: ImageButton = itemView.findViewById(R.id.habit_edit_button) // Add edit button

        fun bind(habit: Habit) {
            nameTextView.text = habit.name
            categoryTextView.text = habit.category
            progressTextView.text = "${habit.progress}/${habit.target}"
            progressBar.max = habit.target
            progressBar.progress = habit.progress
            targetTextView.text = itemView.context.getString(R.string.target) + " ${habit.target} " +
                    itemView.context.getString(R.string.per_day)

            // Remove listener before setting checked state to prevent crash
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = habit.isCompleted
            
            // Set listener after programmatic state change
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onCheckChanged(habit, isChecked)
            }

            // Set up delete button
            deleteButton.setOnClickListener {
                onDeleteHabit(habit)
            }

            // Set up edit button
            editButton.setOnClickListener {
                onEditHabit(habit)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(habits[position])
    }

    override fun getItemCount(): Int = habits.size

    // Method to remove habit from adapter
    fun removeHabit(habit: Habit) {
        val position = habits.indexOfFirst { it.id == habit.id }
        if (position != -1) {
            habits.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}