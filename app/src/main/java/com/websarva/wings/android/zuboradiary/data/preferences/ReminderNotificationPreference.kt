package com.websarva.wings.android.zuboradiary.data.preferences

import java.time.LocalTime

internal class ReminderNotificationPreference {

    companion object {
        const val IS_CHECKED_DEFAULT_VALUE = false
        const val NOTIFICATION_TIME_DEFAULT_VALUE = ""
    }

    val isChecked: Boolean
    val notificationTimeString: String

    val notificationLocalTime: LocalTime?
        get() {
            if (!isChecked) return null
            return LocalTime.parse(notificationTimeString)
        }

    constructor(isChecked: Boolean, notificationTime: LocalTime? = null) {
        if (isChecked) require(notificationTime != null)

        this.isChecked = isChecked
        if (isChecked) {
            this.notificationTimeString = notificationTime.toString()
        } else {
            this.notificationTimeString = ""
        }
    }

    constructor(isChecked: Boolean, notificationTimeString: String) {
        if (isChecked) {
            // 時間文字列形式確認
            LocalTime.parse(notificationTimeString)
        } else {
            require(notificationTimeString.matches("".toRegex()))
        }

        this.isChecked = isChecked
        this.notificationTimeString = notificationTimeString
    }

    constructor(): this(IS_CHECKED_DEFAULT_VALUE, NOTIFICATION_TIME_DEFAULT_VALUE)
}
