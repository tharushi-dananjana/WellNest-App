package com.example.wellnest

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Data class for time slot
data class TimeSlot(
    var time: String,
    var waterTaken: Boolean = false
)

class HydrationFragment : Fragment() {

    private val timeSlots = mutableListOf<TimeSlot>()
    private lateinit var adapter: TimeSlotAdapter
    private var cupSizeLiters = 0.25 // 250 ml

    private val PREFS_NAME = "hydration_prefs"
    private val KEY_TIME_SLOTS = "time_slots"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hydration, container, false)

        val rvTimeSlots = view.findViewById<RecyclerView>(R.id.rvTimeSlots)
        val btnAdd = view.findViewById<Button>(R.id.btnAddTimeSlot)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalWater)
        val switchReminder = view.findViewById<Switch>(R.id.switchReminder)

        // Load saved time slots
        loadTimeSlots()

        // RecyclerView setup
        adapter = TimeSlotAdapter(timeSlots) {
            calculateTotalWater(tvTotal)
            saveTimeSlots()
        }
        rvTimeSlots.layoutManager = LinearLayoutManager(requireContext())
        rvTimeSlots.adapter = adapter

        calculateTotalWater(tvTotal)

        // Add time slot
        btnAdd.setOnClickListener {
            showAddTimeDialog()
        }

        // Reminder switch
        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(requireContext(), "Reminders Enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Reminders Disabled", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun showAddTimeDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add Time Slot")

        val input = EditText(requireContext())
        input.hint = "HH:MM"
        input.inputType = InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_TIME
        builder.setView(input)

        builder.setPositiveButton("Add") { dialog, _ ->
            val time = input.text.toString()
            if (time.isNotEmpty()) {
                timeSlots.add(TimeSlot(time))
                adapter.notifyDataSetChanged()
                saveTimeSlots()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun calculateTotalWater(tv: TextView) {
        val total = timeSlots.count { it.waterTaken } * cupSizeLiters
        tv.text = "Today's Water Intake: $total L"
    }

    // Save time slots to SharedPreferences
    private fun saveTimeSlots() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = Gson().toJson(timeSlots)
        editor.putString(KEY_TIME_SLOTS, json)
        editor.apply()
    }

    // Load time slots from SharedPreferences
    private fun loadTimeSlots() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_TIME_SLOTS, null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<MutableList<TimeSlot>>() {}.type
            val savedSlots: MutableList<TimeSlot> = Gson().fromJson(json, type)
            timeSlots.clear()
            timeSlots.addAll(savedSlots)
        }
    }
}

// Adapter
class TimeSlotAdapter(
    private val slots: MutableList<TimeSlot>,
    private val updateTotal: () -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    inner class TimeSlotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val cbTaken: CheckBox = view.findViewById(R.id.cbTaken)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val slot = slots[position]
        holder.tvTime.text = slot.time
        holder.cbTaken.isChecked = slot.waterTaken

        holder.cbTaken.setOnCheckedChangeListener { _, isChecked ->
            slot.waterTaken = isChecked
            updateTotal()
        }

        // Edit
        holder.btnEdit.setOnClickListener {
            val context = holder.itemView.context
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Edit Time Slot")

            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_TIME
            input.setText(slot.time)
            builder.setView(input)

            builder.setPositiveButton("Save") { dialog, _ ->
                val newTime = input.text.toString()
                if (newTime.isNotEmpty()) {
                    slot.time = newTime
                    notifyItemChanged(position)
                    updateTotal()
                }
                dialog.dismiss()
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        }

        // Delete
        holder.btnDelete.setOnClickListener {
            slots.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, slots.size)
            updateTotal()
        }
    }

    override fun getItemCount(): Int = slots.size
}
