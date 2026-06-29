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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrainerDetailActivity : AppCompatActivity() {

    private lateinit var recyclerWorkout: RecyclerView
    private lateinit var recyclerReviews: RecyclerView
    private lateinit var btnBack: TextView
    private var allWorkouts = listOf<Workout>()
    private var selectedRating = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_trainer_detail)

        val dbHelper = DatabaseHelper(this)
        btnBack = findViewById(R.id.btnBack)

        val imgTrainer =
            findViewById<ImageView>(R.id.imgTrainer)

        val txtName =
            findViewById<TextView>(R.id.txtName)

        val txtSpecialty =
            findViewById<TextView>(R.id.txtSpecialty)

        val txtCalories =
            findViewById<TextView>(R.id.txtCalories)

        val txtStudentCount =
            findViewById<TextView>(R.id.txtStudentCount)

        val txtMuscle =
            findViewById<TextView>(R.id.txtMuscle)

        val txtSchedule =
            findViewById<TextView>(R.id.txtSchedule)

        recyclerWorkout =
            findViewById(R.id.recyclerWorkout)
        recyclerReviews = findViewById(R.id.recyclerReviews)
        recyclerReviews.layoutManager = LinearLayoutManager(this)

        val btnFilterChest = findViewById<LinearLayout>(R.id.btnFilterChest)
        val btnFilterLeg = findViewById<LinearLayout>(R.id.btnFilterLeg)
        val btnFilterBack = findViewById<LinearLayout>(R.id.btnFilterBack)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        recyclerWorkout.layoutManager =
            LinearLayoutManager(this)

        val trainerId =
            intent.getIntExtra("trainer_id", 0)

        val name =
            intent.getStringExtra("trainer_name") ?: ""

        val specialty =
            intent.getStringExtra("trainer_specialty") ?: ""

        val image =
            intent.getStringExtra("trainer_image") ?: ""

        val calories =
            intent.getStringExtra("trainer_calories") ?: ""

        val muscle =
            intent.getStringExtra("trainer_muscle") ?: ""

        val schedule =
            intent.getStringExtra("trainer_schedule") ?: ""

        txtName.text = name
        txtSpecialty.text = specialty
        txtCalories.text = calories
        txtMuscle.text = muscle
        txtSchedule.text = schedule

        lifecycleScope.launch(Dispatchers.IO) {
            val studentCount = dbHelper.getTrainerStudentCount(trainerId)
            withContext(Dispatchers.Main) {
                txtStudentCount.text = "👥 $studentCount người đang theo học"
            }
        }

        // Hide empty fields for Plan mode
        if (specialty.isEmpty()) txtSpecialty.visibility = View.GONE
        if (calories.isEmpty()) txtCalories.visibility = View.GONE
        if (muscle.isEmpty()) txtMuscle.visibility = View.GONE
        if (schedule.isEmpty()) txtSchedule.visibility = View.GONE

        val imageResId = resources.getIdentifier(image, "drawable", packageName)
        if (imageResId != 0) {
            Glide.with(this)
                .load(imageResId)
                .into(imgTrainer)
        } else {
            Glide.with(this)
                .load(R.drawable.male)
                .into(imgTrainer)
        }

        loadWorkouts(trainerId)
        loadReviews(trainerId)
        setupReviewInput(trainerId)
        setupEnrollment(trainerId)

        btnFilterChest.setOnClickListener { filterWorkouts("Ngực") }
        btnFilterLeg.setOnClickListener { filterWorkouts("Chân") }
        btnFilterBack.setOnClickListener { filterWorkouts("Lưng") }

        findViewById<Button>(R.id.btnStartWorkout).setOnClickListener {
            val intent = Intent(this, WorkoutSessionActivity::class.java)
            intent.putExtra("trainer_id", trainerId)
            startActivity(intent)
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
            val isEnrolled = dbHelper.isUserEnrolled(userId, trainerId)
            withContext(Dispatchers.Main) {
                if (isEnrolled) {
                    btnEnroll.text = "ĐÃ ĐĂNG KÝ THEO HỌC"
                    btnEnroll.isEnabled = false
                    btnEnroll.alpha = 0.5f
                }
            }
        }

        btnEnroll.setOnClickListener {
            if (userId == 0) {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch(Dispatchers.IO) {
                val success = dbHelper.enrollTrainer(userId, trainerId)
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(this@TrainerDetailActivity, "Đăng ký khóa học thành công!", Toast.LENGTH_SHORT).show()
                        btnEnroll.text = "ĐÃ ĐĂNG KÝ THEO HỌC"
                        btnEnroll.isEnabled = false
                        btnEnroll.alpha = 0.5f
                    }
                }
            }
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
                stars.forEachIndexed { i, star ->
                    star.alpha = if (i <= index) 1.0f else 0.3f
                }
            }
        }

        btnSubmit.setOnClickListener {
            val comment = edtComment.text.toString().trim()
            if (comment.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = SessionManager(this).getUserId()
            if (userId == 0) {
                Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dbHelper = DatabaseHelper(this)
            if (dbHelper.addReview(userId, trainerId, selectedRating, comment)) {
                Toast.makeText(this, "Gửi đánh giá thành công!", Toast.LENGTH_SHORT).show()
                edtComment.text.clear()
                loadReviews(trainerId)
            }
        }
    }

    private fun filterWorkouts(muscle: String) {
        val filtered = allWorkouts.filter { it.muscle_group.contains(muscle, true) }
        recyclerWorkout.adapter = WorkoutAdapter(filtered)
    }
}
