package com.example.fitbody.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.adapter.ScheduleAdapter
import com.example.fitbody.utils.SessionManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ScheduleFragment : Fragment(R.layout.fragment_schedule) {

    private lateinit var btnBack: TextView
    private lateinit var chipGroupDays: ChipGroup
    private lateinit var chipGroupWorkouts: ChipGroup
    private lateinit var btnAddSchedule: Button
    private lateinit var recyclerSchedule: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBack = view.findViewById(R.id.btnBack)
        chipGroupDays = view.findViewById(R.id.chipGroupDays)
        chipGroupWorkouts = view.findViewById(R.id.chipGroupWorkouts)
        btnAddSchedule = view.findViewById(R.id.btnAddSchedule)
        recyclerSchedule = view.findViewById(R.id.recyclerSchedule)

        btnBack.setOnClickListener {
            requireActivity().finish()
        }

        recyclerSchedule.layoutManager = LinearLayoutManager(requireContext())
        recyclerSchedule.isNestedScrollingEnabled = false

        loadSchedule()

        btnAddSchedule.setOnClickListener {
            addSchedule()
        }
    }

    private fun loadSchedule() {
        val userId = SessionManager(requireContext()).getUserId()
        val dbHelper = DatabaseHelper(requireContext())
        val list = dbHelper.getSchedule(userId)

        recyclerSchedule.adapter = ScheduleAdapter(
            list,
            { schedule -> completeSchedule(schedule.id) },
            { schedule -> deleteSchedule(schedule.id) }
        )
    }

    private fun addSchedule() {
        val selectedDayId = chipGroupDays.checkedChipId
        val selectedWorkoutId = chipGroupWorkouts.checkedChipId

        if (selectedDayId == View.NO_ID || selectedWorkoutId == View.NO_ID) {
            Toast.makeText(requireContext(), "Vui lòng chọn ngày và bài tập", Toast.LENGTH_SHORT).show()
            return
        }

        val dayChip = chipGroupDays.findViewById<Chip>(selectedDayId)
        val workoutChip = chipGroupWorkouts.findViewById<Chip>(selectedWorkoutId)

        val dayName = dayChip.text.toString()
        val workoutPlan = workoutChip.text.toString()

        val userId = SessionManager(requireContext()).getUserId()
        val dbHelper = DatabaseHelper(requireContext())
        val result = dbHelper.addSchedule(userId, dayName, workoutPlan)

        if (result != -1L) {
            Toast.makeText(requireContext(), "Đã thêm lịch tập: $dayName", Toast.LENGTH_SHORT).show()
            loadSchedule()
        }
    }

    private fun completeSchedule(id: Int) {
        val dbHelper = DatabaseHelper(requireContext())
        if (dbHelper.completeSchedule(id)) {
            Toast.makeText(requireContext(), "Đã hoàn thành lịch tập", Toast.LENGTH_SHORT).show()
            loadSchedule()
        }
    }

    private fun deleteSchedule(id: Int) {
        val dbHelper = DatabaseHelper(requireContext())
        if (dbHelper.deleteSchedule(id)) {
            Toast.makeText(requireContext(), "Đã xóa lịch tập", Toast.LENGTH_SHORT).show()
            loadSchedule()
        }
    }
}
