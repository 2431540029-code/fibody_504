package com.example.fitbody.ui

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import java.io.File
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
    private lateinit var btnPause: Button
    private lateinit var btnReset: Button

    private lateinit var layoutSession: LinearLayout
    private lateinit var layoutFinish: LinearLayout
    private lateinit var txtTotalExercises: TextView
    private lateinit var txtTotalTime: TextView
    private lateinit var btnFinish: Button

    private var workouts = listOf<Workout>()
    private var currentIndex = 0
    private var isResting = false
    private var isPaused = false
    private var timer: CountDownTimer? = null
    
    private var timeLeftInMillis: Long = 0
    private val WORK_TIME = 30000L
    private val REST_TIME = 15000L
    private var startTimeMillis: Long = 0
    private var trainerId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_session)

        initViews()

        trainerId = intent.getIntExtra("trainer_id", 1)
        loadWorkouts(trainerId)

        btnBack.setOnClickListener { 
            clearSession()
            finish() 
        }
        btnFinish.setOnClickListener { 
            clearSession()
            finish() 
        }

        btnAction.setOnClickListener {
            if (isPaused) {
                resumeWorkout()
            } else if (timer == null) {
                startTimeMillis = System.currentTimeMillis()
                startNextStep()
            }
        }

        btnPause.setOnClickListener {
            if (!isPaused) pauseWorkout()
            else resumeWorkout()
        }

        btnReset.setOnClickListener {
            showResetOptions()
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
        btnPause = findViewById(R.id.btnPause)
        btnReset = findViewById(R.id.btnReset)

        layoutSession = findViewById(R.id.layoutSession)
        layoutFinish = findViewById(R.id.layoutFinish)
        txtTotalExercises = findViewById(R.id.txtTotalExercises)
        txtTotalTime = findViewById(R.id.txtTotalTime)
        btnFinish = findViewById(R.id.btnFinish)
    }

    private fun loadWorkouts(id: Int) {
        val dbHelper = DatabaseHelper(this)
        lifecycleScope.launch(Dispatchers.IO) {
            workouts = dbHelper.getWorkoutsByTrainer(id)
            withContext(Dispatchers.Main) {
                if (workouts.isEmpty()) {
                    txtWorkoutName.text = "Không có bài tập nào"
                    btnAction.isEnabled = false
                } else {
                    restoreSession()
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
        } else {
            txtStatus.text = "TẬP LUYỆN!"
            txtStatus.setTextColor(getColor(android.R.color.holo_green_light))
        }
        txtWorkoutName.text = targetWorkout.workout_name

        // Logic tải GIF: Kiểm tra xem video_url có phải đường dẫn file hay không
        val gifPath = targetWorkout.video_url
        if (gifPath.startsWith("/")) {
            // Tải từ file hệ thống (PT thêm vào)
            Glide.with(this).asGif().load(File(gifPath)).into(imgWorkoutGif)
        } else {
            // Tải từ resource raw (Dữ liệu mẫu)
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
                .load(if (resId != 0) resId else android.R.color.transparent)
                .placeholder(R.drawable.ic_launcher_background)
                .into(imgWorkoutGif)
        }

        if (isPaused) {
            imgWorkoutGif.alpha = 0.5f
        } else {
            imgWorkoutGif.alpha = 1.0f
        }

        if (!isResting && currentIndex < workouts.size - 1) {
            layoutNext.visibility = View.VISIBLE
            txtNextWorkoutName.text = workouts[currentIndex + 1].workout_name
        } else {
            layoutNext.visibility = View.INVISIBLE
        }
    }

    private fun pauseWorkout() {
        timer?.cancel()
        isPaused = true
        btnPause.text = "▶ TIẾP TỤC"
        btnPause.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(android.R.color.holo_blue_light)))
        imgWorkoutGif.alpha = 0.5f
        saveSession()
        Toast.makeText(this, "Đã dừng tập luyện", Toast.LENGTH_SHORT).show()
    }

    private fun resumeWorkout() {
        isPaused = false
        btnPause.text = "⏸ DỪNG"
        btnPause.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(android.R.color.holo_red_light)))
        imgWorkoutGif.alpha = 1.0f
        startTimer(timeLeftInMillis)
        
        btnAction.visibility = View.GONE
        btnPause.visibility = View.VISIBLE
        btnReset.visibility = View.VISIBLE
    }

    private fun showResetOptions() {
        val options = arrayOf("Tập lại bài này", "Tập lại từ đầu buổi")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Lựa chọn tập lại")
            .setItems(options) { _, which ->
                if (which == 0) {
                    resetCurrentWorkout()
                } else {
                    resetEntireSession()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun resetCurrentWorkout() {
        timer?.cancel()
        isPaused = false
        timeLeftInMillis = if (isResting) REST_TIME else WORK_TIME
        startTimer(timeLeftInMillis)
        updateUI()
        
        btnAction.visibility = View.GONE
        btnPause.visibility = View.VISIBLE
        btnReset.visibility = View.VISIBLE
        btnPause.text = "⏸ DỪNG"
        btnPause.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(android.R.color.holo_red_light)))
        Toast.makeText(this, "Đã bắt đầu lại bài tập này", Toast.LENGTH_SHORT).show()
    }

    private fun resetEntireSession() {
        timer?.cancel()
        clearSession()
        currentIndex = 0
        isResting = false
        isPaused = false
        startTimeMillis = System.currentTimeMillis()
        
        updateUI()
        startTimer(WORK_TIME)
        
        btnAction.visibility = View.GONE
        btnPause.visibility = View.VISIBLE
        btnReset.visibility = View.VISIBLE
        btnPause.text = "⏸ DỪNG"
        btnPause.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(android.R.color.holo_red_light)))
        Toast.makeText(this, "Đã bắt đầu lại từ bài tập đầu tiên", Toast.LENGTH_SHORT).show()
    }

    private fun startNextStep() {
        if (currentIndex >= workouts.size) {
            showFinishScreen()
            return
        }

        btnAction.visibility = View.GONE
        btnPause.visibility = View.VISIBLE
        btnReset.visibility = View.VISIBLE

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

    private fun startTimer(time: Long) {
        timer?.cancel()
        timeLeftInMillis = time
        timer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
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

    private fun saveSession() {
        val sp = getSharedPreferences("workout_session", Context.MODE_PRIVATE)
        sp.edit().apply {
            putInt("trainer_id", trainerId)
            putInt("current_index", currentIndex)
            putBoolean("is_resting", isResting)
            putBoolean("is_paused", isPaused)
            putLong("time_left", timeLeftInMillis)
            putLong("start_time", startTimeMillis)
            apply()
        }
    }

    private fun restoreSession() {
        val sp = getSharedPreferences("workout_session", Context.MODE_PRIVATE)
        val savedId = sp.getInt("trainer_id", -1)
        
        if (savedId == trainerId) {
            currentIndex = sp.getInt("current_index", 0)
            isResting = sp.getBoolean("is_resting", false)
            isPaused = sp.getBoolean("is_paused", false)
            timeLeftInMillis = sp.getLong("time_left", 0L)
            startTimeMillis = sp.getLong("start_time", 0L)

            if (timeLeftInMillis > 0) {
                txtTimer.text = String.format(Locale.getDefault(), "00:%02d", timeLeftInMillis / 1000)
                if (isPaused) {
                    btnPause.visibility = View.VISIBLE
                    btnReset.visibility = View.VISIBLE
                    btnPause.text = "▶ TIẾP TỤC"
                    btnAction.visibility = View.GONE
                }
            }
        }
    }

    private fun clearSession() {
        val sp = getSharedPreferences("workout_session", Context.MODE_PRIVATE)
        sp.edit().clear().apply()
    }

    private fun showFinishScreen() {
        timer?.cancel()
        clearSession()
        layoutSession.visibility = View.GONE
        layoutFinish.visibility = View.VISIBLE
        
        txtTotalExercises.text = workouts.size.toString()
        
        val durationMillis = System.currentTimeMillis() - startTimeMillis
        val minutes = (durationMillis / 1000) / 60
        val seconds = (durationMillis / 1000) % 60
        txtTotalTime.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        
        // TỰ ĐỘNG LƯU BUỔI TẬP VÀO THỐNG KÊ
        val userId = com.example.fitbody.utils.SessionManager(this).getUserId()
        val dbHelper = DatabaseHelper(this)
        dbHelper.addCheckIn(userId, "COMPLETED_SESSION_${trainerId}")
        
        NotificationHelper.showNotification(this)
    }

    override fun onStop() {
        super.onStop()
        if (timer != null && !layoutFinish.isShown) {
            saveSession()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
