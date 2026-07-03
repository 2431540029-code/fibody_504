package com.example.fitbody.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.utils.SessionManager
import java.util.Locale

class WorkoutStatsActivity : AppCompatActivity() {

    private lateinit var txtTotalWorkouts: TextView
    private lateinit var txtTotalMinutes: TextView
    private lateinit var txtTotalCalories: TextView
    private lateinit var txtStreak: TextView
    private lateinit var txtMotivation: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_stats)

        initViews()
        loadRealStats()

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun initViews() {
        txtTotalWorkouts = findViewById(R.id.txtTotalWorkouts)
        txtTotalMinutes = findViewById(R.id.txtTotalMinutes)
        txtTotalCalories = findViewById(R.id.txtTotalCalories)
        txtStreak = findViewById(R.id.txtStreak)
        txtMotivation = findViewById(R.id.txtMotivation)
        findViewById<TextView>(R.id.txtTitle).text = "Thống Kê Cá Nhân"
    }

    private fun loadRealStats() {
        val userId = SessionManager(this).getUserId()
        val dbHelper = DatabaseHelper(this)
        
        // Dữ liệu thật từ Database
        val checkInList = dbHelper.getCheckInHistoryList(userId)
        val totalSessions = checkInList.size
        
        // Tính toán dựa trên dữ liệu thật
        val totalMinutes = totalSessions * 45 // Giả định 45p/buổi
        val totalCalories = totalSessions * 300 // Giả định 300kcal/buổi
        val streak = if (totalSessions > 0) totalSessions else 0 // Giả định streak đơn giản

        txtTotalWorkouts.text = totalSessions.toString()
        txtTotalMinutes.text = String.format(Locale.getDefault(), "%d Phút", totalMinutes)
        txtTotalCalories.text = String.format(Locale.getDefault(), "%d Kcal", totalCalories)
        txtStreak.text = String.format(Locale.getDefault(), "%d Ngày 🔥", streak)
        
        // Lời khuyên động viên
        txtMotivation.text = when {
            totalSessions == 0 -> "Hành trình vạn dặm bắt đầu từ bước chân đầu tiên. Hãy thực hiện buổi tập ngay hôm nay!"
            totalSessions < 5 -> "Khởi đầu tuyệt vời! Hãy duy trì thêm 3 buổi nữa để hình thành thói quen nhé."
            totalSessions < 15 -> "Bạn đang làm rất tốt! Cơ thể bạn đang trong quá trình chuyển hóa mạnh mẽ."
            else -> "Phong độ đỉnh cao! Bạn đang truyền cảm hứng cho cộng đồng FitBody đấy."
        }
    }
}
