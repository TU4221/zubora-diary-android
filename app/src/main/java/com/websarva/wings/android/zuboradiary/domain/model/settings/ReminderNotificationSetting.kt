package com.websarva.wings.android.zuboradiary.domain.model.settings

import java.time.LocalTime

// MEMO:通知時間は有効の時のみ必要になる為、下記シールドクラスで対応。
internal sealed class ReminderNotificationSetting(
    val isEnabled: Boolean
) : UserSetting {

    data class Enabled(val notificationTime: LocalTime) : ReminderNotificationSetting(true)

    data object Disabled : ReminderNotificationSetting(false)
}
