package com.example.fitbody.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.adapter.WorkoutAdapter
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PTHomeFragment : Fragment() {

    private lateinit var txtPtWelcome: TextView
    private lateinit var txtTotalStudents: TextView
    private lateinit var txtTotalWorkouts: TextView
    private lateinit var rvRecentWorkouts: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pt_home, container, false)
        
        txtPtWelcome = view.findViewById(R.id.txtPtWelcome)
        txtTotalStudents = view.findViewById(R.id.txtTotalStudents)
        txtTotalWorkouts = view.findViewById(R.id.txtTotalWorkouts)
        rvRecentWorkouts = view.findViewById(R.id.rvRecentWorkouts)
        
        rvRecentWorkouts.layoutManager = LinearLayoutManager(requireContext())
        
        loadDashboardData()
        
        return view
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val session = SessionManager(requireContext())
        val ptUsername = session.getUsername()
        val dbHelper = DatabaseHelper(requireContext())

        txtPtWelcome.text = "Chào HLV, $ptUsername"

        lifecycleScope.launch(Dispatchers.IO) {
            val realTrainerId = dbHelper.getTrainerIdByUsername(ptUsername)
            
            if (realTrainerId != 0) {
                val studentCount = dbHelper.getTrainerStudentCount(realTrainerId)
                val workouts = dbHelper.getWorkoutsByTrainer(realTrainerId)
                
                withContext(Dispatchers.Main) {
                    txtTotalStudents.text = studentCount.toString()
                    txtTotalWorkouts.text = workouts.size.toString()
                    rvRecentWorkouts.adapter = WorkoutAdapter(workouts.take(5))
                }
            } else {
                withContext(Dispatchers.Main) {
                    txtTotalStudents.text = "0"
                    txtTotalWorkouts.text = "0"
                }
            }
        }
    }
}
