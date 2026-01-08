package com.example.pulsepath.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pulsepath.R
import com.example.pulsepath.adapters.HabitAdapter
import com.example.pulsepath.databinding.FragmentHabitsBinding
import com.example.pulsepath.databinding.DialogAddHabitBinding
import com.example.pulsepath.models.Habit
import com.example.pulsepath.utils.DataManager
import java.util.UUID

class HabitsFragment : Fragment() {

    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var dataManager: DataManager
    private val habitList = mutableListOf<Habit>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataManager = DataManager(requireContext())
        setupRecyclerView()
        loadHabits()
        setupFloatingActionButton()
        setupSwipeToDelete()
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(habitList,
            onCheckChanged = { habit, isChecked ->
                updateHabitCompletion(habit, isChecked)
            },
            onDeleteHabit = { habit ->
                showDeleteHabitDialog(habit)
            },
            onEditHabit = { habit ->
                showEditHabitDialog(habit)
            }
        )

        binding.habitsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitAdapter
            setHasFixedSize(true)
        }
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
                val habit = habitList[position]
                showDeleteHabitDialog(habit)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.habitsRecyclerView)
    }

    private fun showDeleteHabitDialog(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_delete))
            .setMessage(getString(R.string.delete_habit_message))
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                deleteHabit(habit)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                habitAdapter.notifyDataSetChanged() // Refresh to undo swipe
                dialog.dismiss()
            }
            .setOnCancelListener {
                habitAdapter.notifyDataSetChanged() // Refresh to undo swipe
            }
            .show()
    }

    private fun deleteHabit(habit: Habit) {
        habitList.removeAll { it.id == habit.id }
        habitAdapter.notifyDataSetChanged()
        saveHabits()
        updateEmptyState()
        Toast.makeText(requireContext(), "Habit deleted", Toast.LENGTH_SHORT).show()
    }

    private fun loadHabits() {
        habitList.clear()
        habitList.addAll(dataManager.loadHabits())
        habitAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (habitList.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.habitsRecyclerView.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.habitsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupFloatingActionButton() {
        binding.addHabitFab.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun showAddHabitDialog() {
        showHabitDialog(null) // null means new habit
    }

    private fun showEditHabitDialog(habit: Habit) {
        showHabitDialog(habit) // habit means edit existing
    }

    private fun showHabitDialog(habit: Habit?) {
        val dialogBinding = DialogAddHabitBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (habit == null) getString(R.string.add_new_habit) else getString(R.string.edit_habit))
            .setView(dialogBinding.root)
            .setPositiveButton(if (habit == null) getString(R.string.add) else getString(R.string.save), null)
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        // Pre-fill fields if editing
        habit?.let {
            dialogBinding.habitNameEditText.setText(it.name)
            dialogBinding.habitCategoryEditText.setText(it.category)
            dialogBinding.habitTargetEditText.setText(it.target.toString())
        }

        // Set custom positive button click listener
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val name = dialogBinding.habitNameEditText.text.toString().trim()
                val category = dialogBinding.habitCategoryEditText.text.toString().trim()
                val targetText = dialogBinding.habitTargetEditText.text.toString()
                val target = if (targetText.isNotEmpty()) targetText.toInt() else 1

                if (name.isNotBlank()) {
                    if (habit == null) {
                        // Add new habit
                        val newHabit = Habit(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            category = if (category.isNotBlank()) category else "General",
                            target = target.coerceAtLeast(1),
                            progress = 0
                        )
                        habitList.add(newHabit)
                        habitAdapter.notifyItemInserted(habitList.size - 1)
                        Toast.makeText(requireContext(), "Habit added successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Edit existing habit
                        val habitIndex = habitList.indexOfFirst { it.id == habit.id }
                        if (habitIndex != -1) {
                            habitList[habitIndex].name = name
                            habitList[habitIndex].category = if (category.isNotBlank()) category else "General"
                            habitList[habitIndex].target = target.coerceAtLeast(1)
                            habitAdapter.notifyItemChanged(habitIndex)
                            Toast.makeText(requireContext(), "Habit updated successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    updateEmptyState()
                    saveHabits()
                    dialog.dismiss()
                } else {
                    dialogBinding.habitNameEditText.error = getString(R.string.error_required_field)
                }
            }
        }

        dialog.show()
    }

    private fun updateHabitCompletion(habit: Habit, isChecked: Boolean) {
        val habitIndex = habitList.indexOfFirst { it.id == habit.id }
        if (habitIndex != -1) {
            val updatedHabit = habitList[habitIndex]
            val previousProgress = updatedHabit.progress
            val currentProgress = if (isChecked) {
                updatedHabit.progress + 1
            } else {
                updatedHabit.progress - 1
            }
            updatedHabit.progress = currentProgress.coerceIn(0, updatedHabit.target)
            
            // Check if habit just completed its target
            if (isChecked && previousProgress < updatedHabit.target && updatedHabit.progress >= updatedHabit.target) {
                showCongratulationsDialog(updatedHabit)
            }
            
            habitAdapter.notifyItemChanged(habitIndex)
            saveHabits()
        }
    }
    
    private fun showCongratulationsDialog(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("🎉 " + getString(R.string.congratulations))
            .setMessage(getString(R.string.habit_completed_message, habit.name))
            .setPositiveButton(getString(R.string.awesome)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun saveHabits() {
        dataManager.saveHabits(habitList)
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when fragment becomes visible
        loadHabits()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): HabitsFragment {
            return HabitsFragment()
        }
    }
}