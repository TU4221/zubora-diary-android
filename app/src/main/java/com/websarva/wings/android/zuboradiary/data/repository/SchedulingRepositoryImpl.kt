package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.mapper.scheduling.SchedulingRepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.worker.NotificationSchedulingDataSource
import com.websarva.wings.android.zuboradiary.domain.repository.SchedulingRepository
import java.time.LocalTime
import javax.inject.Inject

internal class SchedulingRepositoryImpl @Inject constructor(
    private val notificationSchedulingDataSource: NotificationSchedulingDataSource
) : SchedulingRepository {

    override suspend fun registerReminderNotification(settingTime: LocalTime) {
        try {
            notificationSchedulingDataSource.registerReminderNotificationWorker(settingTime)
        } catch (e: Exception) {
            throw SchedulingRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun cancelReminderNotification() {
        try {
            notificationSchedulingDataSource.cancelReminderNotificationWorker()
        } catch (e: Exception) {
            throw SchedulingRepositoryExceptionMapper.toDomainException(e)
        }
    }
}
