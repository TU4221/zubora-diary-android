package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.worker.NotificationSchedulingDataSource
import com.websarva.wings.android.zuboradiary.data.worker.WorkProfileAccessFailureException
import com.websarva.wings.android.zuboradiary.domain.repository.SchedulingRepository
import com.websarva.wings.android.zuboradiary.domain.repository.exception.SchedulingException
import java.time.LocalTime

internal class SchedulingRepositoryImpl(
    private val notificationSchedulingDataSource: NotificationSchedulingDataSource
) : SchedulingRepository {

    override fun registerReminderNotification(settingTime: LocalTime) {
        try {
            notificationSchedulingDataSource.registerReminderNotificationWorker(settingTime)
        } catch (e: WorkProfileAccessFailureException) {
            throw SchedulingException(cause = e)
        }
    }

    override fun cancelReminderNotification() {
        try {
            notificationSchedulingDataSource.cancelReminderNotificationWorker()
        } catch (e: WorkProfileAccessFailureException) {
            throw SchedulingException(cause = e)
        }
    }
}
