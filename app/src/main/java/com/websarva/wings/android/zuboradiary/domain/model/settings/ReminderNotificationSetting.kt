package com.websarva.wings.android.zuboradiary.domain.model.settings

import java.time.LocalTime

// TODO:シールドクラスに変換
internal data class ReminderNotificationSetting(
    val isChecked: Boolean,
    val notificationTime: LocalTime? = null
) : UserSetting {

    init {
        if (isChecked) {
            requireNotNull(notificationTime)
        } else {
            require(notificationTime == null)
        }
    }
}
