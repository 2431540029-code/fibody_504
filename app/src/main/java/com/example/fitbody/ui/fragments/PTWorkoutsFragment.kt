package com.example.fitbody.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.adapter.WorkoutAdapter
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.model.Workout
import com.example.fitbody.ui.pt.AddWorkoutActivity
import com.example.fitbody.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PTWorkoutsFragment : Fragment() {

    private lateinit var rvWorkouts: RecyclerView
    private lateinit var btnAdd: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pt_workouts, container, false)
        
        rvWorkouts = view.findViewById(R.id.rvPtWorkouts)
        btnAdd = view.findViewById(R.id.btnAddWorkoutPt)
        
        btnAdd.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val dbHelper = DatabaseHelper(requireContext())
                val realTrainerId = dbHelper.getTrainerIdByUsername(SessionManager(requireContext()).getUsername())
                withContext(Dispatchers.Main) {
                    val intent = Intent(requireContext(), AddWorkoutActivity::class.java)
                    intent.putExtra("trainer_id", realTrainerId)
                    startActivity(intent)
                }
            }
        }
        
        loadWorkouts()
        
        return view
    }

    override fun onResume() {
        super.onResume()
        loadWorkouts()
    }

    private fun loadWorkouts() {
        val ptUsername = SessionManager(requireContext()).getUsername()
        val dbHelper = DatabaseHelper(requireContext())
        
        lifecycleScope.launch(Dispatchers.IO) {
            val realTrainerId = dbHelper.getTrainerIdByUsername(ptUsername)
            if (realTrainerId != 0) {
                val workouts = dbHelper.getWorkoutsByTrainer(realTrainerId)
                withContext(Dispatchers.Main) {
                    rvWorkouts.adapter = WorkoutAdapter(workouts) { workout ->
                        showDeleteDialog(workout)
                    }
                }
            }
        }
    }

    private fun showDeleteDialog(workout: Workout) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa bài tập")
            .setMessage("Bạn có chắc muốn xóa bài tập '${workout.workout_name}' không?")
            .setPositiveButton("Xóa") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    if (DatabaseHelper(requireContext()).deleteWorkout(workout.id)) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Đã xóa", Toast.LENGTH_SHORT).show()
                            loadWorkouts()
                        }
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}
