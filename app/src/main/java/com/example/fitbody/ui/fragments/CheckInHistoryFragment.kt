package com.example.fitbody.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.adapter.CheckInAdapter
import com.example.fitbody.utils.SessionManager

class CheckInHistoryFragment : Fragment(R.layout.fragment_checkin_history) {

    private lateinit var btnBack: TextView
    private lateinit var recyclerCheckIn: RecyclerView

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        btnBack = view.findViewById(R.id.btnBack)
        recyclerCheckIn = view.findViewById(R.id.recyclerCheckIn)

        val checkInList = getCheckInHistory()

        recyclerCheckIn.layoutManager =
            LinearLayoutManager(requireContext())

        recyclerCheckIn.adapter =
            CheckInAdapter(checkInList)

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun getCheckInHistory(): List<String> {
        val session = SessionManager(requireContext())
        val userId = session.getUserId()
        
        val dbHelper = DatabaseHelper(requireContext())
        val history = dbHelper.getCheckInHistoryList(userId)

        return if (history.isEmpty()) {
            listOf("Chưa có lịch sử check-in")
        } else {
            history.map { "${it.checkin_time}" }
        }
    }
}