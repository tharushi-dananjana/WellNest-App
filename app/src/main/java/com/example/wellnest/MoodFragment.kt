package com.example.wellnest

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MoodFragment : Fragment() {

    private lateinit var adapter: MoodAdapter
    private var selectedEmoji: String = "😊"

    private lateinit var etMoodNote: EditText
    private lateinit var recyclerMoods: RecyclerView
    private lateinit var btnSaveMood: Button

    private val moods: MutableList<Mood> by lazy {
        MoodStorage.loadMoods(requireContext()) // load saved moods
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood, container, false)

        etMoodNote = view.findViewById(R.id.et_mood_note)
        recyclerMoods = view.findViewById(R.id.recycler_moods)
        btnSaveMood = view.findViewById(R.id.btn_save_mood)

        val emojiHappy: TextView = view.findViewById(R.id.emoji_happy)
        val emojiSad: TextView = view.findViewById(R.id.emoji_sad)
        val emojiAngry: TextView = view.findViewById(R.id.emoji_angry)
        val emojiRelaxed: TextView = view.findViewById(R.id.emoji_relaxed)

        adapter = MoodAdapter(moods,
            onEdit = { mood -> editMood(mood) },
            onDelete = { MoodStorage.saveMoods(requireContext(), moods) }
        )

        recyclerMoods.layoutManager = LinearLayoutManager(requireContext())
        recyclerMoods.adapter = adapter

        // Emoji selection
        emojiHappy.setOnClickListener { selectedEmoji = "😊" }
        emojiSad.setOnClickListener { selectedEmoji = "😢" }
        emojiAngry.setOnClickListener { selectedEmoji = "😡" }
        emojiRelaxed.setOnClickListener { selectedEmoji = "😌" }

        btnSaveMood.setOnClickListener {
            val note = etMoodNote.text.toString()
            if (note.isNotEmpty()) addMood(selectedEmoji, note)
        }

        return view
    }

    private fun addMood(emoji: String, note: String) {
        val newMood = Mood(id = moods.size + 1, emoji = emoji, note = note)
        moods.add(0, newMood)
        adapter.notifyItemInserted(0)
        MoodStorage.saveMoods(requireContext(), moods) // save after add
        etMoodNote.text.clear()
    }

    private fun editMood(mood: Mood) {
        etMoodNote.setText(mood.note)
        selectedEmoji = mood.emoji
        btnSaveMood.setOnClickListener {
            mood.note = etMoodNote.text.toString()
            mood.emoji = selectedEmoji
            adapter.notifyDataSetChanged()
            MoodStorage.saveMoods(requireContext(), moods) // save after edit
            etMoodNote.text.clear()
        }
    }
}

/* MoodStorage එක */
object MoodStorage {
    private const val PREF_NAME = "mood_prefs"
    private const val KEY_MOODS = "moods_list"

    fun saveMoods(context: Context, moods: List<Mood>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = Gson().toJson(moods)
        editor.putString(KEY_MOODS, json)
        editor.apply()
    }

    fun loadMoods(context: Context): MutableList<Mood> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_MOODS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Mood>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }
}
