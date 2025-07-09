package com.websarva.wings.android.zuboradiary.data.mapper.preference

import com.websarva.wings.android.zuboradiary.data.preferences.ReminderNotificationPreference
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import java.time.DateTimeException
import java.time.LocalTime

internal fun ReminderNotificationPreference.toDomainModel(): ReminderNotificationSetting {
    return try {
        val notificationTime = LocalTime.parse(notificationTimeString)
        ReminderNotificationSetting(isChecked, notificationTime)
    } catch (e: DateTimeException) {
        ReminderNotificationSetting(isChecked)
    }
}

internal fun ReminderNotificationSetting.toDataModel(): ReminderNotificationPreference {
    return ReminderNotificationPreference(isChecked, notificationTime.toString())
}
