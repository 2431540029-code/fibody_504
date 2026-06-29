package com.example.fitbody.ui.pt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.adapter.WorkoutAdapter
import com.example.fitbody.model.Workout

class PTDashboardActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    private lateinit var rvWorkout: RecyclerView
    private lateinit var txtTotalWorkout: TextView
    private lateinit var txtFavorite: TextView
    private lateinit var btnAdd: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_pt_dashboard)

        db = DatabaseHelper(this)

        rvWorkout = findViewById(R.id.rvWorkout)
        txtTotalWorkout = findViewById(R.id.txtTotalWorkout)
        txtFavorite = findViewById(R.id.txtFavorite)
        btnAdd = findViewById(R.id.btnAddWorkout)

        rvWorkout.layoutManager = LinearLayoutManager(this)

        loadDashboardData()

        btnAdd.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AddWorkoutActivity::class.java
                )
            )
        }
    }

    fun loadDashboardData() {
        val workouts = db.getAllWorkouts()

        // Chapter 4.1: Long Click to Delete with Confirmation Dialog
        rvWorkout.adapter = WorkoutAdapter(workouts) { workout ->
            showDeleteDialog(workout)
        }

        txtTotalWorkout.text = "Tổng bài tập: ${db.getWorkoutCount()}"
        txtFavorite.text = "PT được yêu thích nhất: ${db.getTopFavoriteTrainer()}"
    }

    private fun showDeleteDialog(workout: Workout) {
        AlertDialog.Builder(this)
            .setTitle("Xóa bài tập")
            .setMessage("Bạn có chắc muốn xóa bài tập '${workout.workout_name}' không?")
            .setPositiveButton("Xóa") { _, _ ->
                if (db.deleteWorkout(workout.id)) {
                    Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show()
                    loadDashboardData()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }
}
