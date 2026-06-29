package com.example.fitbody.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.fitbody.utils.NotificationHelper

class DailyReminderWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        // Gửi thông báo nhắc nhở tập luyện (Chapter 4.4 - WorkManager)
        NotificationHelper.showNotification(
            applicationContext,
            "FitBody Nhắc Bạn",
            "Đã đến lúc tập luyện rồi! Đừng quên lịch tập hôm nay nhé. 💪"
        )
        return Result.success()
    }
}
