package com.example.fitbody.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.model.Workout
import com.example.fitbody.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class WorkoutSessionActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var txtStatus: TextView
    private lateinit var txtWorkoutName: TextView
    private lateinit var txtTimer: TextView
    private lateinit var imgWorkoutGif: ImageView
    private lateinit var layoutNext: LinearLayout
    private lateinit var txtNextWorkoutName: TextView
    private lateinit var btnAction: Button

    // Finish Layout components
    private lateinit var layoutSession: LinearLayout
    private lateinit var layoutFinish: LinearLayout
    private lateinit var txtTotalExercises: TextView
    private lateinit var txtTotalTime: TextView
    private lateinit var btnFinish: Button

    private var workouts = listOf<Workout>()
    private var currentIndex = 0
    private var isResting = false
    private var timer: CountDownTimer? = null
    
    private val WORK_TIME = 30000L
    private val REST_TIME = 15000L
    private var startTimeMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_session)

        initViews()

        val trainerId = intent.getIntExtra("trainer_id", 1)
        loadWorkouts(trainerId)

        btnBack.setOnClickListener { finish() }
        btnFinish.setOnClickListener { finish() }

        btnAction.setOnClickListener {
            if (timer == null) {
                startTimeMillis = System.currentTimeMillis()
                startNextStep()
                btnAction.visibility = View.GONE
            }
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        txtStatus = findViewById(R.id.txtStatus)
        txtWorkoutName = findViewById(R.id.txtWorkoutName)
        txtTimer = findViewById(R.id.txtTimer)
        imgWorkoutGif = findViewById(R.id.imgWorkoutGif)
        layoutNext = findViewById(R.id.layoutNext)
        txtNextWorkoutName = findViewById(R.id.txtNextWorkoutName)
        btnAction = findViewById(R.id.btnAction)

        layoutSession = findViewById(R.id.layoutSession)
        layoutFinish = findViewById(R.id.layoutFinish)
        txtTotalExercises = findViewById(R.id.txtTotalExercises)
        txtTotalTime = findViewById(R.id.txtTotalTime)
        btnFinish = findViewById(R.id.btnFinish)
    }

    private fun loadWorkouts(trainerId: Int) {
        val dbHelper = DatabaseHelper(this)
        lifecycleScope.launch(Dispatchers.IO) {
            workouts = dbHelper.getWorkoutsByTrainer(trainerId)
            withContext(Dispatchers.Main) {
                if (workouts.isEmpty()) {
                    txtWorkoutName.text = "Không có bài tập nào"
                    btnAction.isEnabled = false
                } else {
                    updateUI()
                }
            }
        }
    }

    private fun updateUI() {
        if (currentIndex >= workouts.size) return

        val displayIndex = if (isResting && currentIndex + 1 < workouts.size) currentIndex + 1 else currentIndex
        val targetWorkout = workouts[displayIndex]
        
        if (isResting) {
            txtStatus.text = "CHUẨN BỊ BÀI TIẾP THEO"
            txtStatus.setTextColor(getColor(android.R.color.holo_orange_light))
            txtWorkoutName.text = targetWorkout.workout_name
        } else {
            txtStatus.text = "TẬP LUYỆN!"
            txtStatus.setTextColor(getColor(android.R.color.holo_green_light))
            txtWorkoutName.text = targetWorkout.workout_name
        }

        // Tự động làm sạch tên để tìm file GIF (bỏ dấu, thay khoảng trắng)
        val cleanName = targetWorkout.workout_name.lowercase()
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
            .load(if (resId != 0) resId else R.raw.bat_nhay)
            .into(imgWorkoutGif)

        if (!isResting && currentIndex < workouts.size - 1) {
            layoutNext.visibility = View.VISIBLE
            txtNextWorkoutName.text = workouts[currentIndex + 1].workout_name
        } else {
            layoutNext.visibility = View.INVISIBLE
        }
    }

    private fun startNextStep() {
        if (currentIndex >= workouts.size) {
            showFinishScreen()
            return
        }

        if (isResting) {
            isResting = false
            currentIndex++
            if (currentIndex < workouts.size) {
                updateUI()
                startTimer(WORK_TIME)
            } else {
                showFinishScreen()
            }
        } else {
            updateUI()
            startTimer(WORK_TIME)
        }
    }

    private fun showFinishScreen() {
        timer?.cancel()
        layoutSession.visibility = View.GONE
        layoutFinish.visibility = View.VISIBLE
        
        txtTotalExercises.text = workouts.size.toString()
        
        val durationMillis = System.currentTimeMillis() - startTimeMillis
        val minutes = (durationMillis / 1000) / 60
        val seconds = (durationMillis / 1000) % 60
        txtTotalTime.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        
        NotificationHelper.showNotification(this)
    }

    private fun startTimer(time: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                txtTimer.text = String.format(Locale.getDefault(), "00:%02d", seconds)
            }

            override fun onFinish() {
                if (!isResting) {
                    if (currentIndex < workouts.size - 1) {
                        isResting = true
                        updateUI()
                        startTimer(REST_TIME)
                    } else {
                        currentIndex++
                        startNextStep()
                    }
                } else {
                    startNextStep()
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
