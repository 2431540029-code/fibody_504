package com.example.fitbody.ui.pt

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.model.Workout
import java.io.File
import java.io.FileOutputStream

class EditWorkoutActivity : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtSets: EditText
    private lateinit var edtReps: EditText
    private lateinit var edtMuscle: EditText
    private lateinit var txtGifPath: TextView
    private lateinit var btnSave: Button
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    
    private var workoutId: Int = -1
    private var selectedGifUri: Uri? = null
    private var currentGifPath: String? = null

    private val pickGif = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedGifUri = uri
            txtGifPath.text = "Mới chọn: " + getFileName(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_workout)

        workoutId = intent.getIntExtra("workout_id", -1)
        val trainerId = intent.getIntExtra("trainer_id", 0)

        toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Chỉnh sửa bài tập"
        toolbar.setNavigationOnClickListener { finish() }
        
        edtName = findViewById(R.id.edtName)
        edtSets = findViewById(R.id.edtSets)
        edtReps = findViewById(R.id.edtReps)
        edtMuscle = findViewById(R.id.edtMuscle)
        txtGifPath = findViewById(R.id.txtGifPath)
        btnSave = findViewById(R.id.btnSave)
        
        btnSave.text = "Cập nhật bài tập"

        // Load data
        edtName.setText(intent.getStringExtra("workout_name"))
        edtSets.setText(intent.getStringExtra("sets"))
        edtReps.setText(intent.getStringExtra("reps"))
        edtMuscle.setText(intent.getStringExtra("muscle"))
        currentGifPath = intent.getStringExtra("video_url")
        
        if (!currentGifPath.isNullOrEmpty()) {
            txtGifPath.text = if (currentGifPath!!.startsWith("/")) "Đã có GIF (từ máy)" else "Dùng GIF hệ thống"
        }

        findViewById<LinearLayout>(R.id.btnSelectGif).setOnClickListener {
            pickGif.launch("image/gif")
        }

        btnSave.setOnClickListener {
            updateWorkout(trainerId)
        }
    }

    private fun updateWorkout(trainerId: Int) {
        val name = edtName.text.toString().trim()
        val sets = edtSets.text.toString().trim()
        val reps = edtReps.text.toString().trim()
        val muscle = edtMuscle.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên bài tập", Toast.LENGTH_SHORT).show()
            return
        }

        // Nếu có chọn GIF mới thì lưu file mới
        var finalGifPath = currentGifPath ?: ""
        selectedGifUri?.let { uri ->
            val savedPath = saveGifToInternalStorage(uri)
            if (savedPath != null) finalGifPath = savedPath
        }

        val workout = Workout(
            id = workoutId,
            trainer_id = trainerId,
            workout_name = name,
            sets_count = sets,
            reps_count = reps,
            muscle_group = muscle,
            video_url = finalGifPath
        )

        val dbHelper = DatabaseHelper(this)
        if (dbHelper.updateWorkout(workout)) { 
             Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
             finish()
        } else {
             Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
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
}
