package com.example.fitbody.model

data class Progress(
    val id: Int,
    val user_id: Int,
    val workout_count: Int,
    val calories_burned: Int,
    val streak_days: Int,
    val progress_percent: Int
)