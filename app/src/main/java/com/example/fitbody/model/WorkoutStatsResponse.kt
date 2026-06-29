package com.example.fitbody.model

data class WorkoutStatsResponse(
    val success: Boolean,
    val total_workouts: Int,
    val total_calories: Int,
    val streak_days: Int,
    val month_progress: Int
)