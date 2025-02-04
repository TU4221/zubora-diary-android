package com.websarva.wings.android.zuboradiary.data.worker

import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.websarva.wings.android.zuboradiary.worker.ReminderNotificationWorker
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkerRepository @Inject constructor(private val workManager: WorkManager) {

    private val workTagReminderNotification = "ReminderNotification"
    private val uniqueWorkNameReminderNotification = workTagReminderNotification

    fun registerReminderNotificationWorker(settingTime: LocalTime) {
        cancelReminderNotificationWorker()

        val nowTime = LocalTime.now()
        val initialDelaySeconds = calculationBetweenSeconds(nowTime, settingTime)
        Log.d("NotificationWorker", "initialDelaySeconds:$initialDelaySeconds")
        val request =
            PeriodicWorkRequest
                .Builder(ReminderNotificationWorker::class.java, 1, TimeUnit.DAYS)
                .addTag(workTagReminderNotification)
                .setInitialDelay(initialDelaySeconds, TimeUnit.SECONDS)
                .build()
        workManager.enqueueUniquePeriodicWork(
            uniqueWorkNameReminderNotification,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request
        )
    }

    private fun calculationBetweenSeconds(startTime: LocalTime, endTime: LocalTime): Long {
        val betweenSeconds = ChronoUnit.SECONDS.between(startTime, endTime)
        if (betweenSeconds < 0) {
            return 60 /*seconds*/ * 60 /*minutes*/ * 24 /*hours*/ + betweenSeconds
        }
        return betweenSeconds
    }

    fun cancelReminderNotificationWorker() {
        workManager.apply {
            cancelAllWorkByTag(workTagReminderNotification)
            cancelUniqueWork(uniqueWorkNameReminderNotification)
        }

    }
}
