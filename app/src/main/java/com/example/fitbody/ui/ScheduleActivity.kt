package com.example.fitbody.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fitbody.R
import com.example.fitbody.ui.fragments.ScheduleFragment

class ScheduleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ScheduleFragment())
                .commit()
        }
    }
}
