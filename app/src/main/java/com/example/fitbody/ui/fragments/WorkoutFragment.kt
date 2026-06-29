package com.example.fitbody.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.fitbody.R
import com.example.fitbody.ui.WorkoutTimerActivity

class WorkoutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(
            R.layout.fragment_workout,
            container,
            false
        )

        val btnTimer =
            view.findViewById<Button>(
                R.id.btnTimer
            )

        btnTimer.setOnClickListener {

            startActivity(

                Intent(
                    requireContext(),
                    WorkoutTimerActivity::class.java
                )
            )
        }

        return view
    }
}