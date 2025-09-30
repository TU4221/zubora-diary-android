package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.mapper.scheduling.SchedulingRepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.worker.NotificationSchedulingDataSource
import com.websarva.wings.android.zuboradiary.data.worker.exception.WorkerOperationException
import com.websarva.wings.android.zuboradiary.domain.repository.SchedulingRepository
import java.time.LocalTime

internal class SchedulingRepositoryImpl(
    private val notificationSchedulingDataSource: NotificationSchedulingDataSource,
    private val schedulingRepositoryExceptionMapper: SchedulingRepositoryExceptionMapper
) : SchedulingRepository {

    override suspend fun registerReminderNotification(settingTime: LocalTime) {
        try {
            notificationSchedulingDataSource.registerReminderNotificationWorker(settingTime)
        } catch (e: WorkerOperationException) {
            throw schedulingRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun cancelReminderNotification() {
        try {
            notificationSchedulingDataSource.cancelReminderNotificationWorker()
        } catch (e: WorkerOperationException) {
            throw schedulingRepositoryExceptionMapper.toDomainException(e)
        }
    }
}
