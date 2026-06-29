package com.example.fitbody.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.model.Workout
import com.example.fitbody.ui.WorkoutTimerActivity
import com.example.fitbody.ui.pt.EditWorkoutActivity
import com.example.fitbody.utils.SessionManager

class WorkoutDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var webViewVideo: WebView
    private lateinit var imgWorkout: ImageView
    private lateinit var txtWorkoutName: TextView
    private lateinit var txtMuscle: TextView
    private lateinit var txtSets: TextView
    private lateinit var txtReps: TextView
    private lateinit var txtGuide: TextView
    private lateinit var btnStartTimer: Button
    private lateinit var btnVideo: Button

    private var workoutId: Int = -1
    private var trainerId: Int = 0
    private var currentWorkout: Workout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_detail)

        workoutId = intent.getIntExtra("workout_id", -1)
        trainerId = intent.getIntExtra("trainer_id", 0)

        initViews()
        setupListeners()
        
        loadWorkoutData()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        webViewVideo = findViewById(R.id.webViewVideo)
        imgWorkout = findViewById(R.id.imgWorkout)
        txtWorkoutName = findViewById(R.id.txtWorkoutName)
        txtMuscle = findViewById(R.id.txtMuscle)
        txtSets = findViewById(R.id.txtSets)
        txtReps = findViewById(R.id.txtReps)
        txtGuide = findViewById(R.id.txtGuide)
        btnStartTimer = findViewById(R.id.btnStartTimer)
        btnVideo = findViewById(R.id.btnVideo)

        val session = SessionManager(this)
        if (session.getRole() == "pt") {
            btnStartTimer.text = "Chỉnh sửa bài tập"
            btnStartTimer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")))
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnStartTimer.setOnClickListener {
            val session = SessionManager(this)
            if (session.getRole() == "pt") {
                val intent = Intent(this, EditWorkoutActivity::class.java)
                intent.putExtra("workout_id", workoutId)
                intent.putExtra("trainer_id", trainerId)
                currentWorkout?.let {
                    intent.putExtra("workout_name", it.workout_name)
                    intent.putExtra("sets", it.sets_count)
                    intent.putExtra("reps", it.reps_count)
                    intent.putExtra("muscle", it.muscle_group)
                    intent.putExtra("video_url", it.video_url)
                }
                startActivity(intent)
            } else {
                val intent = Intent(this, WorkoutTimerActivity::class.java)
                currentWorkout?.let {
                    intent.putExtra("workout_name", it.workout_name)
                    intent.putExtra("muscle", it.muscle_group)
                    intent.putExtra("video_url", it.video_url)
                }
                startActivity(intent)
            }
        }

        btnVideo.setOnClickListener {
            val videoUrl = currentWorkout?.video_url ?: ""
            if (videoUrl.isEmpty()) {
                Toast.makeText(this, "Bài tập này chưa có video hướng dẫn", Toast.LENGTH_SHORT).show()
            } else {
                imgWorkout.visibility = View.GONE
                webViewVideo.visibility = View.VISIBLE
                setupWebView(videoUrl)
            }
        }
    }

    private fun loadWorkoutData() {
        val db = DatabaseHelper(this)
        currentWorkout = db.getWorkoutById(workoutId)

        currentWorkout?.let { workout ->
            txtWorkoutName.text = workout.workout_name
            txtSets.text = "Sets: ${workout.sets_count}"
            txtReps.text = "Reps: ${workout.reps_count}"
            txtMuscle.text = "Nhóm cơ: ${workout.muscle_group}"
            txtGuide.text = "Giữ form đúng, siết cơ khi tập và nghỉ 60 giây giữa mỗi set."

            // Load ảnh động/GIF
            val cleanName = workout.workout_name.lowercase()
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
                .load(if (resId != 0) resId else R.drawable.ic_launcher_background)
                .into(imgWorkout)
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload data to show changes after editing
        loadWorkoutData()
    }

    private fun setupWebView(url: String) {
        webViewVideo.settings.javaScriptEnabled = true
        webViewVideo.settings.domStorageEnabled = true
        webViewVideo.webChromeClient = WebChromeClient()
        webViewVideo.webViewClient = WebViewClient()

        var finalUrl = url
        if (url.contains("youtube.com/watch?v=")) {
            val videoId = url.split("v=")[1].split("&")[0]
            finalUrl = "https://www.youtube.com/embed/$videoId"
        } else if (url.contains("youtu.be/")) {
            val videoId = url.split("youtu.be/")[1].split("?")[0]
            finalUrl = "https://www.youtube.com/embed/$videoId"
        }

        webViewVideo.loadUrl(finalUrl)
    }

    override fun onDestroy() {
        webViewVideo.destroy()
        super.onDestroy()
    }
}
