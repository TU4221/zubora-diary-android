package com.websarva.wings.android.zuboradiary.data.mapper.settings

import com.websarva.wings.android.zuboradiary.data.preferences.ReminderNotificationPreference
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import java.time.LocalTime

internal fun ReminderNotificationPreference.toDomainModel(): ReminderNotificationSetting {
    return if (isEnabled) {
        val notificationTime = LocalTime.parse(notificationTimeString)
        ReminderNotificationSetting.Enabled(notificationTime)
    } else {
        ReminderNotificationSetting.Disabled
    }
}

internal fun ReminderNotificationSetting.toDataModel(): ReminderNotificationPreference {
    return when (this) {
        is ReminderNotificationSetting.Enabled -> {
            ReminderNotificationPreference(isEnabled, notificationTime.toString())
        }
        ReminderNotificationSetting.Disabled -> {
            ReminderNotificationPreference(isEnabled, "")
        }
    }
}
