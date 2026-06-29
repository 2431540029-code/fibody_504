package com.example.fitbody.ui.pt

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.model.Workout

class EditWorkoutActivity : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtSets: EditText
    private lateinit var edtReps: EditText
    private lateinit var edtMuscle: EditText
    private lateinit var edtVideo: EditText
    private lateinit var btnSave: Button
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    
    private var workoutId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_workout) // Reuse the same layout as Add

        workoutId = intent.getIntExtra("workout_id", -1)
        val trainerId = intent.getIntExtra("trainer_id", 0)

        toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Chỉnh sửa bài tập"
        toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Since we are reusing the layout, we find the views
        edtName = findViewById(R.id.edtName)
        edtSets = findViewById(R.id.edtSets)
        edtReps = findViewById(R.id.edtReps)
        edtMuscle = findViewById(R.id.edtMuscle)
        edtVideo = findViewById(R.id.edtVideo)
        btnSave = findViewById(R.id.btnSave)
        
        btnSave.text = "Cập nhật bài tập"

        // Load existing data
        edtName.setText(intent.getStringExtra("workout_name"))
        edtSets.setText(intent.getStringExtra("sets"))
        edtReps.setText(intent.getStringExtra("reps"))
        edtMuscle.setText(intent.getStringExtra("muscle"))
        edtVideo.setText(intent.getStringExtra("video_url"))

        btnSave.setOnClickListener {
            if (edtName.text.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên bài tập", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val workout = Workout(
                id = workoutId,
                trainer_id = trainerId,
                workout_name = edtName.text.toString(),
                sets_count = edtSets.text.toString(),
                reps_count = edtReps.text.toString(),
                muscle_group = edtMuscle.text.toString(),
                video_url = edtVideo.text.toString()
            )

            val dbHelper = DatabaseHelper(this)
            if (dbHelper.updateWorkout(workout)) { 
                 Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                 finish()
            } else {
                 Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
