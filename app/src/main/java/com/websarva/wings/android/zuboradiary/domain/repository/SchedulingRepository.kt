package com.websarva.wings.android.zuboradiary.domain.repository

import com.websarva.wings.android.zuboradiary.domain.exception.reminder.ReminderNotificationCancellationFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.ReminderNotificationRegistrationFailureException
import java.time.LocalTime

internal interface SchedulingRepository {

    @Throws(ReminderNotificationRegistrationFailureException::class)
    fun registerReminderNotification(settingTime: LocalTime)

    @Throws(ReminderNotificationCancellationFailureException::class)
    fun cancelReminderNotification()
}
