package com.example.fitbody.model

data class LoginResponse(
    val success: Boolean,
    val role: String?,
    val user_id: Int?,
    val username: String?,
    val email: String?
)