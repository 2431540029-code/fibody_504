package com.example.fitbody.model

data class LeaderboardUser(
    val username: String,
    val workoutCount: Int,
    val avatar: String? = null
)
