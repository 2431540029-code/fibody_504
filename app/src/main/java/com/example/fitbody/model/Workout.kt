package com.example.fitbody.model

data class Workout(

    val id: Int,

    val trainer_id: Int,

    val workout_name: String,

    val sets_count: String,

    val reps_count: String,

    val muscle_group: String,

    val video_url: String
)