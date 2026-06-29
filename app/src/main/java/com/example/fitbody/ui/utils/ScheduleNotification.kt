package com.example.fitbody.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object ScheduleNotification {

    fun scheduleMorning(
        context: Context
    ) {

        val intent = Intent(
            context,
            WorkoutReceiver::class.java
        )

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

        val calendar =
            Calendar.getInstance()

        calendar.set(
            Calendar.HOUR_OF_DAY,
            6
        )

        calendar.set(
            Calendar.MINUTE,
            0
        )

        calendar.set(
            Calendar.SECOND,
            0
        )

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        alarmManager.setRepeating(

            AlarmManager.RTC_WAKEUP,

            calendar.timeInMillis,

            AlarmManager.INTERVAL_DAY,

            pendingIntent
        )
    }

    fun scheduleEvening(
        context: Context
    ) {

        val intent = Intent(
            context,
            WorkoutReceiver::class.java
        )

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                1,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

        val calendar =
            Calendar.getInstance()

        calendar.set(
            Calendar.HOUR_OF_DAY,
            18
        )

        calendar.set(
            Calendar.MINUTE,
            0
        )

        calendar.set(
            Calendar.SECOND,
            0
        )

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        alarmManager.setRepeating(

            AlarmManager.RTC_WAKEUP,

            calendar.timeInMillis,

            AlarmManager.INTERVAL_DAY,

            pendingIntent
        )
    }
}