package com.example.fitbody.ui.pt

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbody.R
import com.example.fitbody.model.Workout
import java.io.File
import java.io.FileOutputStream

class AddWorkoutActivity : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtSets: EditText
    private lateinit var edtReps: EditText
    private lateinit var edtMuscle: EditText
    private lateinit var txtGifPath: TextView
    private var selectedGifUri: Uri? = null
    private var internalGifPath: String? = null

    private val pickGif = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedGifUri = uri
            txtGifPath.text = "Đã chọn: " + getFileName(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_workout)

        val trainerId = intent.getIntExtra("trainer_id", 0)

        edtName = findViewById(R.id.edtName)
        edtSets = findViewById(R.id.edtSets)
        edtReps = findViewById(R.id.edtReps)
        edtMuscle = findViewById(R.id.edtMuscle)
        txtGifPath = findViewById(R.id.txtGifPath)

        findViewById<LinearLayout>(R.id.btnSelectGif).setOnClickListener {
            pickGif.launch("image/gif")
        }

        val btnSave = findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener {
            saveWorkout(trainerId)
        }

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

    private fun saveWorkout(trainerId: Int) {
        val name = edtName.text.toString().trim()
        val sets = edtSets.text.toString().trim()
        val reps = edtReps.text.toString().trim()
        val muscle = edtMuscle.text.toString().trim()

        if (name.isEmpty() || sets.isEmpty() || reps.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        // Copy GIF to internal storage if selected
        selectedGifUri?.let { uri ->
            internalGifPath = saveGifToInternalStorage(uri)
        }

        val workout = Workout(
            id = 0,
            trainer_id = trainerId,
            workout_name = name,
            sets_count = sets,
            reps_count = reps,
            muscle_group = muscle,
            video_url = internalGifPath ?: "" // Store the file path in video_url column
        )

        val success = com.example.fitbody.database.DatabaseHelper(this).addWorkout(workout)
        if (success) {
            Toast.makeText(this, "Thêm bài tập thành công", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Lỗi khi lưu bài tập", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveGifToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = "workout_" + System.currentTimeMillis() + ".gif"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String {
        return uri.path?.substringAfterLast('/') ?: "file_gif"
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Hủy bỏ thay đổi")
            .setMessage("Bạn có chắc chắn muốn thoát mà không lưu không?")
            .setPositiveButton("Thoát") { _, _ -> finish() }
            .setNegativeButton("Ở lại", null)
            .show()
    }
}
