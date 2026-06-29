package com.example.fitbody.ui.pt

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.adapter.StudentAdapter
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.model.User // Đảm bảo đã import đúng Model User

class MyStudentsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_students)

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        val recyclerStudents = findViewById<RecyclerView>(R.id.recyclerStudents)
        recyclerStudents.layoutManager = LinearLayoutManager(this)

        val trainerId = intent.getIntExtra("trainer_id", 0)
        val dbHelper = DatabaseHelper(this)

        val students: List<User> = dbHelper.getStudentsForTrainer(trainerId)

        recyclerStudents.adapter = StudentAdapter(students)
    }
}