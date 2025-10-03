package com.example.wellnest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MoodAdapter(
    private val moods: MutableList<Mood>,
    private val onEdit: (Mood) -> Unit,
    private val onDelete: (Mood) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    inner class MoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emojiTv: TextView = view.findViewById(R.id.tv_mood_emoji)
        val noteTv: TextView = view.findViewById(R.id.tv_mood_note)
        val editBtn: ImageButton = view.findViewById(R.id.btnEdit)
        val deleteBtn: ImageButton = view.findViewById(R.id.btnDelete)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moods[position]
        holder.emojiTv.text = mood.emoji
        holder.noteTv.text = mood.note

        holder.editBtn.setOnClickListener { onEdit.invoke(mood) }
        holder.deleteBtn.setOnClickListener {
            moods.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, moods.size)
            onDelete.invoke(mood)
        }
    }

    override fun getItemCount() = moods.size
}
