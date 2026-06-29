package com.example.fitbody

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fitbody.ui.fragments.FavoriteFragment
import com.example.fitbody.ui.fragments.HomeFragment
import com.example.fitbody.ui.fragments.ProfileFragment
import com.example.fitbody.utils.ScheduleNotification
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    private var userId: Int = 0
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        val session = com.example.fitbody.utils.SessionManager(this)
        val targetMode = if (session.isDarkMode()) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userId = intent.getIntExtra("user_id", 0)
        username = intent.getStringExtra("username") ?: ""

        ScheduleNotification.scheduleMorning(this)
        ScheduleNotification.scheduleEvening(this)

        bottomNav = findViewById(R.id.bottomNav)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_favorite -> {
                    replaceFragment(FavoriteFragment())
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        com.example.fitbody.utils.SessionManager(this).updateLastActive()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }
}
