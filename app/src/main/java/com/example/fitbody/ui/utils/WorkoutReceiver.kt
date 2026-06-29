package com.example.fitbody.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WorkoutReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        NotificationHelper
            .showNotification(context)
    }
}