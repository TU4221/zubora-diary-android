package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.worker.NotificationSchedulingDataSource
import com.websarva.wings.android.zuboradiary.data.worker.WorkProfileAccessFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.ReminderNotificationCancellationFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.ReminderNotificationRegistrationFailureException
import com.websarva.wings.android.zuboradiary.domain.repository.SchedulingRepository
import java.time.LocalTime

internal class SchedulingRepositoryImpl(
    private val notificationSchedulingDataSource: NotificationSchedulingDataSource
) : SchedulingRepository {

    override fun registerReminderNotification(settingTime: LocalTime) {
        try {
            notificationSchedulingDataSource.registerReminderNotificationWorker(settingTime)
        } catch (e: WorkProfileAccessFailureException) {
            // WorkManagerが未初期化、または内部状態が不正な場合に発生しうるためキャッチ
            throw ReminderNotificationRegistrationFailureException(e)
        }
    }

    override fun cancelReminderNotification() {
        try {
            notificationSchedulingDataSource.cancelReminderNotificationWorker()
        } catch (e: WorkProfileAccessFailureException) {
            // WorkManagerが未初期化、または内部状態が不正な場合に発生しうるためキャッチ
            throw ReminderNotificationCancellationFailureException(e)
        }
    }
}
