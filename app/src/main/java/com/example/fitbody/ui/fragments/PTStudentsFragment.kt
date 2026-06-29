package com.example.fitbody.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.adapter.StudentAdapter
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PTStudentsFragment : Fragment() {

    private lateinit var rvStudents: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pt_students, container, false)
        
        rvStudents = view.findViewById(R.id.rvPtStudents)
        rvStudents.layoutManager = LinearLayoutManager(requireContext())
        
        loadStudents()
        
        return view
    }

    private fun loadStudents() {
        val ptUsername = SessionManager(requireContext()).getUsername()
        val dbHelper = DatabaseHelper(requireContext())
        
        lifecycleScope.launch(Dispatchers.IO) {
            val realTrainerId = dbHelper.getTrainerIdByUsername(ptUsername)
            if (realTrainerId != 0) {
                val students = dbHelper.getStudentsForTrainer(realTrainerId)
                withContext(Dispatchers.Main) {
                    rvStudents.adapter = StudentAdapter(students)
                }
            }
        }
    }
}
