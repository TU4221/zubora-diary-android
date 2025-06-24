package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.worker.ReminderNotificationWorkManager
import com.websarva.wings.android.zuboradiary.domain.model.error.WorkerError
import java.time.LocalTime

internal class WorkerRepository (private val workManager: ReminderNotificationWorkManager) {

    @Throws(WorkerError.RegisterReminderNotification::class)
    fun registerReminderNotificationWorker(settingTime: LocalTime) {
        try {
            workManager.registerReminderNotificationWorker(settingTime)
        } catch (e: IllegalStateException) {
            // WorkManagerが未初期化、または内部状態が不正な場合に発生しうるためキャッチ
            throw WorkerError.RegisterReminderNotification(e)
        }
    }

    @Throws(WorkerError.CancelReminderNotification::class)
    fun cancelReminderNotificationWorker() {
        try {
            workManager.cancelReminderNotificationWorker()
        } catch (e: IllegalStateException) {
            // WorkManagerが未初期化、または内部状態が不正な場合に発生しうるためキャッチ
            throw WorkerError.CancelReminderNotification(e)
        }
    }
}
