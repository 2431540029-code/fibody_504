package com.example.fitbody.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.utils.NotificationHelper

class WorkoutTimerActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView

    private lateinit var txtTimer: TextView
    private lateinit var txtWorkoutName: TextView
    private lateinit var txtMuscle: TextView
    private lateinit var imgWorkoutGif: ImageView
    private lateinit var webViewVideoTimer: WebView

    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnReset: Button

    private var timeLeft: Long = 30000
    private var timer: CountDownTimer? = null
    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_workout_timer)

        btnBack = findViewById(R.id.btnBack)

        txtTimer = findViewById(R.id.txtTimer)
        txtWorkoutName = findViewById(R.id.txtWorkoutName)
        txtMuscle = findViewById(R.id.txtMuscle)
        imgWorkoutGif = findViewById(R.id.imgWorkoutGif)
        webViewVideoTimer = findViewById(R.id.webViewVideoTimer)

        btnStart = findViewById(R.id.btnStart)
        btnPause = findViewById(R.id.btnPause)
        btnReset = findViewById(R.id.btnReset)

        btnBack.setOnClickListener {
            finish()
        }

        val workoutName = intent.getStringExtra("workout_name") ?: "Bài tập"
        val muscle = intent.getStringExtra("muscle") ?: "Nhóm cơ"
        val videoUrl = intent.getStringExtra("video_url") ?: ""

        txtWorkoutName.text = workoutName
        txtMuscle.text = "Nhóm cơ: $muscle"

        // Ưu tiên hiển thị video nếu có link, nếu không thì hiện GIF
        if (videoUrl.isNotEmpty()) {
            imgWorkoutGif.visibility = View.GONE
            webViewVideoTimer.visibility = View.VISIBLE
            setupWebView(videoUrl)
        } else {
            val cleanName = workoutName.lowercase()
                .replace(" ", "_")
                .replace("á|à|ả|ã|ạ|ă|ắ|ằ|ẳ|ẵ|ặ|â|ấ|ầ|ẩ|ẫ|ậ".toRegex(), "a")
                .replace("é|è|ẻ|ẽ|ẹ|ê|ế|ề|ể|ễ|ệ".toRegex(), "e")
                .replace("í|ì|ỉ|ĩ|ị".toRegex(), "i")
                .replace("ó|ò|ỏ|õ|ọ|ô|ố|ồ|ổ|ỗ|ộ|ơ|ớ|ờ|ở|ỡ|ợ".toRegex(), "o")
                .replace("ú|ù|ủ|ũ|ụ|ư|ứ|ừ|ử|ữ|ự".toRegex(), "u")
                .replace("ý|ỳ|ỷ|ỹ|ỵ".toRegex(), "y")
                .replace("đ".toRegex(), "d")

            val resId = resources.getIdentifier(cleanName, "raw", packageName)
            
            Glide.with(this)
                .asGif()
                .load(if (resId != 0) resId else R.raw.bench_press)
                .into(imgWorkoutGif)
        }

        updateTimer()

        btnStart.setOnClickListener {
            startTimer()
        }

        btnPause.setOnClickListener {
            pauseTimer()
        }

        btnReset.setOnClickListener {
            resetTimer()
        }
    }

    private fun setupWebView(url: String) {
        webViewVideoTimer.settings.javaScriptEnabled = true
        webViewVideoTimer.settings.domStorageEnabled = true
        webViewVideoTimer.webChromeClient = WebChromeClient()
        webViewVideoTimer.webViewClient = WebViewClient()

        var finalUrl = url
        if (url.contains("youtube.com/watch?v=")) {
            val videoId = url.split("v=")[1].split("&")[0]
            finalUrl = "https://www.youtube.com/embed/$videoId"
        } else if (url.contains("youtu.be/")) {
            val videoId = url.split("youtu.be/")[1].split("?")[0]
            finalUrl = "https://www.youtube.com/embed/$videoId"
        }

        webViewVideoTimer.loadUrl(finalUrl)
    }

    private fun startTimer() {
        if (isRunning) return

        timer = object : CountDownTimer(timeLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                updateTimer()
            }

            override fun onFinish() {
                txtTimer.text = "HOÀN THÀNH 💪"
                NotificationHelper.showNotification(this@WorkoutTimerActivity)
                isRunning = false
            }
        }.start()

        isRunning = true
    }

    private fun pauseTimer() {
        timer?.cancel()
        isRunning = false
    }

    private fun resetTimer() {
        timer?.cancel()
        timeLeft = 30000
        updateTimer()
        isRunning = false
    }

    private fun updateTimer() {
        val seconds = timeLeft / 1000
        val minutes = seconds / 60
        val remain = seconds % 60
        txtTimer.text = String.format("%02d:%02d", minutes, remain)
    }

    override fun onDestroy() {
        webViewVideoTimer.destroy()
        timer?.cancel()
        super.onDestroy()
    }
}