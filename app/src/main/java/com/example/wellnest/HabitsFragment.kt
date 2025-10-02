package com.example.wellnest

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

data class Habit(var name: String)

class HabitsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HabitAdapter
    private lateinit var habits: MutableList<Habit>

    private val prefsName = "wellness_prefs"
    private val habitsKey = "habits"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_habits, container, false)

        // Load habits
        habits = loadHabits()

        recyclerView = view.findViewById(R.id.recyclerHabits)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = HabitAdapter(habits,
            onEdit = { habit -> editHabitDialog(habit) },
            onDelete = { habit -> deleteHabit(habit) }
        )
        recyclerView.adapter = adapter

        val fabAdd: FloatingActionButton = view.findViewById(R.id.fabAddHabit)
        fabAdd.setOnClickListener { addHabitDialog() }

        return view
    }

    private fun addHabitDialog() {
        val input = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("Add New Habit")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val habit = Habit(input.text.toString())
                habits.add(habit)
                saveHabits()
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editHabitDialog(habit: Habit) {
        val input = EditText(requireContext())
        input.setText(habit.name)
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Habit")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                habit.name = input.text.toString()
                saveHabits()
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteHabit(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete this habit?")
            .setPositiveButton("Yes") { _, _ ->
                habits.remove(habit)
                saveHabits()
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun saveHabits() {
        val prefs = requireContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val set = habits.map { it.name }.toSet()
        prefs.edit().putStringSet(habitsKey, set).apply()
    }

    private fun loadHabits(): MutableList<Habit> {
        val prefs = requireContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(habitsKey, setOf()) ?: setOf()
        return set.map { Habit(it) }.toMutableList()
    }
}
