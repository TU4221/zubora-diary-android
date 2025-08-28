package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.worker.NotificationSchedulingDataSource
import com.websarva.wings.android.zuboradiary.data.worker.WorkProfileAccessFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.ReminderNotificationCancellationFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.ReminderNotificationRegistrationFailureException
import java.time.LocalTime

internal class WorkerRepository (private val workManager: NotificationSchedulingDataSource) {

    @Throws(ReminderNotificationRegistrationFailureException::class)
    fun registerReminderNotificationWorker(settingTime: LocalTime) {
        try {
            workManager.registerReminderNotificationWorker(settingTime)
        } catch (e: WorkProfileAccessFailureException) {
            // WorkManagerが未初期化、または内部状態が不正な場合に発生しうるためキャッチ
            throw ReminderNotificationRegistrationFailureException(e)
        }
    }

    @Throws(ReminderNotificationCancellationFailureException::class)
    fun cancelReminderNotificationWorker() {
        try {
            workManager.cancelReminderNotificationWorker()
        } catch (e: WorkProfileAccessFailureException) {
            // WorkManagerが未初期化、または内部状態が不正な場合に発生しうるためキャッチ
            throw ReminderNotificationCancellationFailureException(e)
        }
    }
}
