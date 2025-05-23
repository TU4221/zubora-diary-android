package com.websarva.wings.android.zuboradiary.data.worker

import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

internal class ReminderNotificationWorkManager (private val workManager: WorkManager)  {

    private val reminderNotificationWorkTag = "ReminderNotification"
    private val reminderNotificationUniqueWorkName = reminderNotificationWorkTag

    private val logTag = createLogTag()

    fun registerReminderNotificationWorker(settingTime: LocalTime) {
        cancelReminderNotificationWorker()

        val nowTime = LocalTime.now()
        val initialDelaySeconds = calculationBetweenSeconds(nowTime, settingTime)
        Log.d(
            logTag,
            "registerReminderNotificationWorker()_initialDelaySeconds = $initialDelaySeconds"
        )
        val request =
            PeriodicWorkRequest
                .Builder(ReminderNotificationWorker::class.java, 1, TimeUnit.DAYS)
                .addTag(reminderNotificationWorkTag)
                .setInitialDelay(initialDelaySeconds, TimeUnit.SECONDS)
                .build()
        workManager.enqueueUniquePeriodicWork(
            reminderNotificationUniqueWorkName,
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
            cancelAllWorkByTag(reminderNotificationWorkTag)
            cancelUniqueWork(reminderNotificationUniqueWorkName)
        }
    }
}
