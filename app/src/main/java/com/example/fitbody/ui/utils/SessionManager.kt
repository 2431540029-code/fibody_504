package com.example.fitbody.utils

import android.content.Context

class SessionManager(
    context: Context
) {

    private val prefs =
        context.getSharedPreferences(
            "fitbody_session",
            Context.MODE_PRIVATE
        )

    fun saveLogin(
        username: String,
        role: String,
        userId: Int
    ) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("username", username)
            .putString("role", role)
            .putInt("user_id", userId)
            .putLong("last_active_time", System.currentTimeMillis())
            .apply()
    }

    fun updateLastActive() {
        prefs.edit()
            .putLong("last_active_time", System.currentTimeMillis())
            .apply()
    }

    fun isSessionExpired(): Boolean {
        if (!isLoggedIn()) return true
        val lastActive = prefs.getLong("last_active_time", 0L)
        val currentTime = System.currentTimeMillis()
        val tenMinutesInMillis = 10 * 60 * 1000
        return (currentTime - lastActive) > tenMinutesInMillis
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(
            "is_logged_in",
            false
        )
    }

    fun getUsername(): String {
        return prefs.getString(
            "username",
            ""
        ) ?: ""
    }

    fun getRole(): String {
        return prefs.getString(
            "role",
            ""
        ) ?: ""
    }

    fun getUserId(): Int {
        return prefs.getInt(
            "user_id",
            0
        )
    }

    fun logout() {
        prefs.edit()
            .remove("is_logged_in")
            .remove("username")
            .remove("role")
            .remove("user_id")
            .remove("last_active_time")
            .apply()
    }

    fun setDarkMode(isDark: Boolean) {
        val userId = getUserId()
        prefs.edit()
            .putBoolean("dark_mode_$userId", isDark)
            .putBoolean("dark_mode_last", isDark) // Store as last used theme for login screen
            .apply()
    }

    fun isDarkMode(): Boolean {
        val userId = getUserId()
        return if (userId != 0) {
            prefs.getBoolean("dark_mode_$userId", prefs.getBoolean("dark_mode_last", false))
        } else {
            prefs.getBoolean("dark_mode_last", false)
        }
    }
}