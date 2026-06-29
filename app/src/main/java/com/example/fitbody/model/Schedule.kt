package com.example.fitbody.model

data class Schedule(
    val id: Int,
    val user_id: Int,
    val day_name: String,
    val workout_plan: String,
    val status: String
)