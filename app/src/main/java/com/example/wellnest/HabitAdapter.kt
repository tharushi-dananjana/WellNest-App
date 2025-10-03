package com.example.wellnest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter for RecyclerView
class HabitAdapter(
    private val habits: MutableList<Habit>,
    private val onEdit: (Habit) -> Unit,
    private val onDelete: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    // ViewHolder class
    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvHabitName)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditHabit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteHabit)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.habit_item, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.tvName.text = habit.name

        // Edit click
        holder.btnEdit.setOnClickListener { onEdit(habit) }

        // Delete click
        holder.btnDelete.setOnClickListener { onDelete(habit) }
    }

    override fun getItemCount(): Int = habits.size
}
