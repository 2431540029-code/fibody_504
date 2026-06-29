package com.example.fitbody.model

data class Review(
    val id: Int,
    val userId: Int,
    val username: String,
    val trainerId: Int,
    val rating: Int,
    val comment: String,
    val date: String
)