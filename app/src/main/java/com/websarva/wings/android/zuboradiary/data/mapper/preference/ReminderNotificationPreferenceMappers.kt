package com.websarva.wings.android.zuboradiary.data.mapper.preference

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.preferences.ReminderNotificationPreference
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.DateTimeException
import java.time.LocalTime

internal fun ReminderNotificationPreference.toDomainModel(): ReminderNotificationSetting {
    return if (isEnabled) {
        try {
            val notificationTime = LocalTime.parse(notificationTimeString)
            ReminderNotificationSetting.Enabled(notificationTime)
        } catch (e: DateTimeException) {
            Log.e(createLogTag(), "リマインダー通知が有効ですが、通知時刻文字列が空なので、無効状態で返します。")
            ReminderNotificationSetting.Disabled
        }
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
            ReminderNotificationPreference(isEnabled)
        }
    }
}
