package com.websarva.wings.android.zuboradiary.data.worker

import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

internal class NotificationSchedulingDataSource(private val workManager: WorkManager) {

    private val reminderNotificationWorkTag = "ReminderNotification"
    private val reminderNotificationUniqueWorkName = reminderNotificationWorkTag

    private val logTag = createLogTag()

    @Throws(WorkProfileAccessFailureException::class)
    private fun executeWorkOperation(
        operation: () -> Unit
    ) {
        try {
            operation()
        } catch (e: IllegalStateException) {
            // WorkManagerが未初期化、または内部状態が不正な場合に発生しうるためキャッチ
            throw WorkProfileAccessFailureException(e)
        }
    }

    @Throws(WorkProfileAccessFailureException::class)
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
        executeWorkOperation {
            workManager.enqueueUniquePeriodicWork(
                reminderNotificationUniqueWorkName,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                request
            )
        }
    }

    private fun calculationBetweenSeconds(startTime: LocalTime, endTime: LocalTime): Long {
        val betweenSeconds = ChronoUnit.SECONDS.between(startTime, endTime)
        if (betweenSeconds < 0) {
            return 60 /*seconds*/ * 60 /*minutes*/ * 24 /*hours*/ + betweenSeconds
        }
        return betweenSeconds
    }

    @Throws(WorkProfileAccessFailureException::class)
    fun cancelReminderNotificationWorker() {
        executeWorkOperation {
            workManager.apply {
                cancelAllWorkByTag(reminderNotificationWorkTag)
                cancelUniqueWork(reminderNotificationUniqueWorkName)
            }
        }
    }
}
