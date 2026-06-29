package com.example.fitbody.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.adapter.ScheduleAdapter
import com.example.fitbody.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScheduleFragment : Fragment(R.layout.fragment_schedule) {

    private lateinit var btnBack: TextView
    private lateinit var edtDayName: EditText
    private lateinit var edtWorkoutPlan: EditText
    private lateinit var btnAddSchedule: Button
    private lateinit var recyclerSchedule: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBack = view.findViewById(R.id.btnBack)
        edtDayName = view.findViewById(R.id.edtDayName)
        edtWorkoutPlan = view.findViewById(R.id.edtWorkoutPlan)
        btnAddSchedule = view.findViewById(R.id.btnAddSchedule)
        recyclerSchedule = view.findViewById(R.id.recyclerSchedule)

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        recyclerSchedule.layoutManager =
            LinearLayoutManager(requireContext())

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
        val dayName = edtDayName.text.toString().trim()
        val workoutPlan = edtWorkoutPlan.text.toString().trim()

        if (dayName.isEmpty() || workoutPlan.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ lịch tập", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = SessionManager(requireContext()).getUserId()
        val dbHelper = DatabaseHelper(requireContext())
        val result = dbHelper.addSchedule(userId, dayName, workoutPlan)

        if (result != -1L) {
            Toast.makeText(requireContext(), "Đã thêm lịch tập", Toast.LENGTH_SHORT).show()
            edtDayName.text.clear()
            edtWorkoutPlan.text.clear()
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