package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.worker.ReminderNotificationWorkManager
import com.websarva.wings.android.zuboradiary.data.worker.WorkProfileAccessException
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.CancelReminderNotificationFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.RegisterReminderNotificationFailedException
import java.time.LocalTime

internal class WorkerRepository (private val workManager: ReminderNotificationWorkManager) {

    @Throws(RegisterReminderNotificationFailedException::class)
    fun registerReminderNotificationWorker(settingTime: LocalTime) {
        try {
            workManager.registerReminderNotificationWorker(settingTime)
        } catch (e: WorkProfileAccessException) {
            // WorkManagerが未初期化、または内部状態が不正な場合に発生しうるためキャッチ
            throw RegisterReminderNotificationFailedException(e)
        }
    }

    @Throws(CancelReminderNotificationFailedException::class)
    fun cancelReminderNotificationWorker() {
        try {
            workManager.cancelReminderNotificationWorker()
        } catch (e: WorkProfileAccessException) {
            // WorkManagerが未初期化、または内部状態が不正な場合に発生しうるためキャッチ
            throw CancelReminderNotificationFailedException(e)
        }
    }
}
