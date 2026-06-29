package com.example.fitbody.model

data class Trainer(

    val id: Int,

    val name: String,

    val specialty: String,

    val muscle: String,

    val calories: String,

    val schedule: String,

    val image: String,

    val description: String,
    
    val likeCount: Int = 0,
    
    val isLikedByMe: Boolean = false
)