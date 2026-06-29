package com.example.fitbody.ui.pt

import android.os.Bundle
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbody.R
import com.example.fitbody.model.Workout

class AddWorkoutActivity : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtSets: EditText
    private lateinit var edtReps: EditText
    private lateinit var edtMuscle: EditText
    private lateinit var edtVideo: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_workout)

        val trainerId = intent.getIntExtra("trainer_id", 0)

        edtName = findViewById(R.id.edtName)
        edtSets = findViewById(R.id.edtSets)
        edtReps = findViewById(R.id.edtReps)
        edtMuscle = findViewById(R.id.edtMuscle)
        edtVideo = findViewById(R.id.edtVideo)

        // Restore state if available (Chapter 4.3 - Handling Activity Re-creation)
        savedInstanceState?.let {
            edtName.setText(it.getString("saved_name"))
            edtMuscle.setText(it.getString("saved_muscle"))
            edtVideo.setText(it.getString("saved_video"))
        }

        val btnSave = findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            if (edtName.text.isEmpty() || edtSets.text.isEmpty() || edtReps.text.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val workout = Workout(
                id = 0,
                trainer_id = trainerId,
                workout_name = edtName.text.toString(),
                sets_count = edtSets.text.toString(),
                reps_count = edtReps.text.toString(),
                muscle_group = edtMuscle.text.toString(),
                video_url = edtVideo.text.toString()
            )

            val success = com.example.fitbody.database.DatabaseHelper(this).addWorkout(workout)

            if (success) {
                Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Thêm thất bại", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Back button with confirmation (Chapter 4.1 - Events)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (edtName.text.isNotEmpty() || edtSets.text.isNotEmpty()) {
                    showExitConfirmation()
                } else {
                    finish()
                }
            }
        })
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Hủy bỏ thay đổi")
            .setMessage("Bạn có chắc chắn muốn thoát mà không lưu không?")
            .setPositiveButton("Thoát") { _, _ -> finish() }
            .setNegativeButton("Ở lại", null)
            .show()
    }

    // Save state before Activity is killed (Chapter 4.3)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("saved_name", edtName.text.toString())
        outState.putString("saved_muscle", edtMuscle.text.toString())
        outState.putString("saved_video", edtVideo.text.toString())
    }
}
