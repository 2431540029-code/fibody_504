package com.example.fitbody.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.adapter.ReviewAdapter
import com.example.fitbody.adapter.WorkoutAdapter
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.model.Review
import com.example.fitbody.model.Workout
import com.example.fitbody.ui.WorkoutSessionActivity
import com.example.fitbody.utils.SessionManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrainerDetailActivity : AppCompatActivity() {

    private lateinit var recyclerWorkout: RecyclerView
    private lateinit var recyclerReviews: RecyclerView
    private lateinit var btnBack: TextView
    private lateinit var txtStudentCount: TextView
    private var allWorkouts = listOf<Workout>()
    private var selectedRating = 5
    private var isEnrolled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainer_detail)

        val dbHelper = DatabaseHelper(this)
        btnBack = findViewById(R.id.btnBack)
        val imgTrainer = findViewById<ImageView>(R.id.imgTrainer)
        val txtName = findViewById<TextView>(R.id.txtName)
        val txtSpecialty = findViewById<TextView>(R.id.txtSpecialty)
        val txtCalories = findViewById<TextView>(R.id.txtCalories)
        txtStudentCount = findViewById(R.id.txtStudentCount)
        val txtMuscle = findViewById<TextView>(R.id.txtMuscle)
        val txtSchedule = findViewById<TextView>(R.id.txtSchedule)

        recyclerWorkout = findViewById(R.id.recyclerWorkout)
        recyclerReviews = findViewById(R.id.recyclerReviews)
        recyclerReviews.layoutManager = LinearLayoutManager(this)

        // Các nút lọc danh mục
        val btnFilterAll = findViewById<LinearLayout>(R.id.btnFilterAll)
        val btnFilterChest = findViewById<LinearLayout>(R.id.btnFilterChest)
        val btnFilterLeg = findViewById<LinearLayout>(R.id.btnFilterLeg)
        val btnFilterBack = findViewById<LinearLayout>(R.id.btnFilterBack)

        btnBack.setOnClickListener { finish() }

        recyclerWorkout.layoutManager = LinearLayoutManager(this)

        val trainerId = intent.getIntExtra("trainer_id", 0)
        val name = intent.getStringExtra("trainer_name") ?: ""
        val specialty = intent.getStringExtra("trainer_specialty") ?: ""
        val image = intent.getStringExtra("trainer_image") ?: ""
        val calories = intent.getStringExtra("trainer_calories") ?: ""
        val muscle = intent.getStringExtra("trainer_muscle") ?: ""
        val schedule = intent.getStringExtra("trainer_schedule") ?: ""

        txtName.text = name
        txtSpecialty.text = specialty
        txtCalories.text = calories
        txtMuscle.text = muscle
        txtSchedule.text = schedule

        updateStudentCount(trainerId)

        if (specialty.isEmpty()) txtSpecialty.visibility = View.GONE
        if (calories.isEmpty()) txtCalories.visibility = View.GONE
        if (muscle.isEmpty()) txtMuscle.visibility = View.GONE
        if (schedule.isEmpty()) txtSchedule.visibility = View.GONE

        val imageResId = resources.getIdentifier(image, "drawable", packageName)
        Glide.with(this).load(if (imageResId != 0) imageResId else R.drawable.male).into(imgTrainer)

        loadWorkouts(trainerId)
        loadReviews(trainerId)
        setupReviewInput(trainerId)
        setupEnrollment(trainerId)

        // Logic lọc thông minh cho 14 HLV
        btnFilterAll.setOnClickListener { 
            updateFilterUI(btnFilterAll)
            recyclerWorkout.adapter = WorkoutAdapter(allWorkouts) 
        }
        btnFilterChest.setOnClickListener { 
            updateFilterUI(btnFilterChest)
            filterWorkouts(listOf("Ngực", "Vai", "Tay", "Combat")) 
        }
        btnFilterLeg.setOnClickListener { 
            updateFilterUI(btnFilterLeg)
            filterWorkouts(listOf("Chân", "Mông", "Đùi")) 
        }
        btnFilterBack.setOnClickListener { 
            updateFilterUI(btnFilterBack)
            filterWorkouts(listOf("Lưng", "Bụng", "Core", "Sức mạnh")) 
        }

        findViewById<Button>(R.id.btnStartWorkout).setOnClickListener {
            val intent = Intent(this, WorkoutSessionActivity::class.java)
            intent.putExtra("trainer_id", trainerId)
            startActivity(intent)
        }
    }

    private fun updateFilterUI(selected: LinearLayout) {
        val buttons = listOf(
            findViewById<LinearLayout>(R.id.btnFilterAll),
            findViewById<LinearLayout>(R.id.btnFilterChest),
            findViewById<LinearLayout>(R.id.btnFilterLeg),
            findViewById<LinearLayout>(R.id.btnFilterBack)
        )
        buttons.forEach { it.setBackgroundResource(R.drawable.bg_card_home) }
        selected.setBackgroundResource(R.drawable.bg_card_service) // Đổi màu để làm nổi bật nút đang chọn
    }

    private fun updateStudentCount(trainerId: Int) {
        val dbHelper = DatabaseHelper(this)
        lifecycleScope.launch(Dispatchers.IO) {
            val count = dbHelper.getTrainerStudentCount(trainerId)
            withContext(Dispatchers.Main) {
                txtStudentCount.text = "👥 $count người đang theo học"
            }
        }
    }

    private fun loadWorkouts(trainerId: Int) {
        val dbHelper = DatabaseHelper(this)
        lifecycleScope.launch(Dispatchers.IO) {
            allWorkouts = dbHelper.getWorkoutsByTrainer(trainerId)
            withContext(Dispatchers.Main) {
                recyclerWorkout.adapter = WorkoutAdapter(allWorkouts)
            }
        }
    }

    private fun setupEnrollment(trainerId: Int) {
        val btnEnroll = findViewById<Button>(R.id.btnEnroll)
        val userId = SessionManager(this).getUserId()
        val dbHelper = DatabaseHelper(this)

        lifecycleScope.launch(Dispatchers.IO) {
            isEnrolled = dbHelper.isUserEnrolled(userId, trainerId)
            withContext(Dispatchers.Main) {
                updateEnrollButton(btnEnroll)
            }
        }

        btnEnroll.setOnClickListener {
            if (userId == 0) {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            lifecycleScope.launch(Dispatchers.IO) {
                val success = if (isEnrolled) dbHelper.unenrollTrainer(userId, trainerId) else dbHelper.enrollTrainer(userId, trainerId)
                if (success) {
                    isEnrolled = !isEnrolled
                    withContext(Dispatchers.Main) {
                        updateEnrollButton(btnEnroll)
                        updateStudentCount(trainerId)
                        Toast.makeText(this@TrainerDetailActivity, if (isEnrolled) "Đã theo dõi!" else "Đã hủy theo dõi!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateEnrollButton(btn: Button) {
        if (isEnrolled) {
            btn.text = "ĐANG THEO DÕI"
            btn.setBackgroundColor(android.graphics.Color.parseColor("#333333"))
        } else {
            btn.text = "ĐĂNG KÝ THEO HỌC"
            btn.setBackgroundColor(android.graphics.Color.parseColor("#00E676"))
        }
    }

    private fun loadReviews(trainerId: Int) {
        val dbHelper = DatabaseHelper(this)
        lifecycleScope.launch(Dispatchers.IO) {
            val reviews = dbHelper.getReviewsForTrainer(trainerId)
            withContext(Dispatchers.Main) {
                recyclerReviews.adapter = ReviewAdapter(reviews)
            }
        }
    }

    private fun setupReviewInput(trainerId: Int) {
        val stars = arrayOf(
            findViewById<TextView>(R.id.star1),
            findViewById<TextView>(R.id.star2),
            findViewById<TextView>(R.id.star3),
            findViewById<TextView>(R.id.star4),
            findViewById<TextView>(R.id.star5)
        )
        val edtComment = findViewById<EditText>(R.id.edtComment)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitReview)

        stars.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                selectedRating = index + 1
                stars.forEach { it.alpha = 0.3f }
                for (i in 0..index) stars[i].alpha = 1.0f
            }
        }

        btnSubmit.setOnClickListener {
            val comment = edtComment.text.toString().trim()
            if (comment.isEmpty()) return@setOnClickListener

            val userId = SessionManager(this).getUserId()
            if (userId == 0) return@setOnClickListener

            val dbHelper = DatabaseHelper(this)
            if (dbHelper.addReview(userId, trainerId, selectedRating, comment)) {
                Toast.makeText(this, "Đã gửi đánh giá", Toast.LENGTH_SHORT).show()
                edtComment.text.clear()
                loadReviews(trainerId)
            }
        }
    }

    private fun filterWorkouts(keywords: List<String>) {
        val filtered = allWorkouts.filter { workout ->
            keywords.any { target -> workout.muscle_group.contains(target, ignoreCase = true) }
        }
        
        if (filtered.isEmpty()) {
            Toast.makeText(this, "HLV này chưa có bài tập trong mục này", Toast.LENGTH_SHORT).show()
            recyclerWorkout.adapter = WorkoutAdapter(allWorkouts) // Reset nếu không có
        } else {
            recyclerWorkout.adapter = WorkoutAdapter(filtered)
        }
    }
}
