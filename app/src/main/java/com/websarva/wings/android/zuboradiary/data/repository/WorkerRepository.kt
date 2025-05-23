package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.worker.ReminderNotificationWorkManager
import java.time.LocalTime

internal class WorkerRepository (private val workManager: ReminderNotificationWorkManager) {

    fun registerReminderNotificationWorker(settingTime: LocalTime) {
        workManager.registerReminderNotificationWorker(settingTime)
    }

    fun cancelReminderNotificationWorker() {
        workManager.cancelReminderNotificationWorker()
    }
}
