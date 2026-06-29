package com.example.fitbody.ui

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.utils.SessionManager

class WorkoutStatsActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var txtTitle: TextView
    private lateinit var txtTotalWorkout: TextView
    private lateinit var txtTotalCalories: TextView
    private lateinit var txtStreak: TextView
    private lateinit var txtMonthPercent: TextView
    private lateinit var txtAdvice: TextView
    private lateinit var progressMonth: ProgressBar

    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_stats)

        btnBack = findViewById(R.id.btnBack)
        txtTitle = findViewById(R.id.txtTitle)
        txtTotalWorkout = findViewById(R.id.txtTotalWorkout)
        txtTotalCalories = findViewById(R.id.txtTotalCalories)
        txtStreak = findViewById(R.id.txtStreak)
        txtMonthPercent = findViewById(R.id.txtMonthPercent)
        txtAdvice = findViewById(R.id.txtAdvice)
        progressMonth = findViewById(R.id.progressMonth)

        txtTitle.text = "Tiến độ tập luyện"

        val session = SessionManager(this)
        userId = session.getUserId()

        btnBack.setOnClickListener {
            finish()
        }

        if (userId == 0) {
            Toast.makeText(
                this,
                "Bạn cần đăng nhập lại",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        loadWorkoutStats()
    }

    private fun loadWorkoutStats() {
        val dbHelper = DatabaseHelper(this)
        val data = dbHelper.getWorkoutStats(userId)

        txtTotalWorkout.text = data.total_workouts.toString()
        txtTotalCalories.text = data.total_calories.toString()
        txtStreak.text = "${data.streak_days} ngày 🔥"
        txtMonthPercent.text = "${data.month_progress}% mục tiêu tháng"
        progressMonth.progress = data.month_progress

        txtAdvice.text = when {
            data.total_workouts == 0 ->
                "Bạn chưa có buổi tập nào. Hãy bắt đầu check-in buổi đầu tiên nhé."

            data.month_progress >= 80 ->
                "Rất tốt! Bạn đang gần hoàn thành mục tiêu tháng."

            data.month_progress >= 50 ->
                "Bạn đang làm khá tốt, hãy duy trì lịch tập đều hơn."

            else ->
                "Bạn nên tập đều hơn để cải thiện tiến độ tháng."
        }
    }
}