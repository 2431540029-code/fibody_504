package com.example.fitbody

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.fitbody.ui.fragments.PTHomeFragment
import com.example.fitbody.ui.fragments.PTProfileFragment
import com.example.fitbody.ui.fragments.PTStudentsFragment
import com.example.fitbody.ui.fragments.PTWorkoutsFragment
import com.example.fitbody.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class PtMainActivity : AppCompatActivity() {

    private lateinit var bottomNavPt: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        val session = SessionManager(this)
        val targetMode = if (session.isDarkMode()) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode)
        }
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pt_main)

        bottomNavPt = findViewById(R.id.bottomNavPt)

        if (savedInstanceState == null) {
            replaceFragment(PTHomeFragment())
        }

        bottomNavPt.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_pt_home -> {
                    replaceFragment(PTHomeFragment())
                    true
                }
                R.id.nav_pt_workouts -> {
                    replaceFragment(PTWorkoutsFragment())
                    true
                }
                R.id.nav_pt_students -> {
                    replaceFragment(PTStudentsFragment())
                    true
                }
                R.id.nav_pt_profile -> {
                    replaceFragment(PTProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SessionManager(this).updateLastActive()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayoutPt, fragment)
            .commit()
    }
}
