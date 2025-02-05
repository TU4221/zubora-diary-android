package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.time.LocalTime

class ReminderNotificationPreference {

    companion object {
        @JvmField
        val PREFERENCES_KEY_IS_CHECKED: Preferences.Key<Boolean> =
            booleanPreferencesKey("is_checked_reminder_notification")
        @JvmField
        val PREFERENCES_KEY_TIME: Preferences.Key<String> =
            stringPreferencesKey("reminder_notification_time")
    }

    val isChecked: Boolean
    private val notificationTimeString: String

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

    @JvmOverloads
    constructor(isChecked: Boolean = false, notificationTime: String = "") {
        if (isChecked) {
            // 時間文字列形式確認
            LocalTime.parse(notificationTime)
        } else {
            require(notificationTime.matches("".toRegex()))
        }

        this.isChecked = isChecked
        this.notificationTimeString = notificationTime
    }

    fun setUpPreferences(mutablePreferences: MutablePreferences) {
        mutablePreferences[PREFERENCES_KEY_IS_CHECKED] = isChecked
        mutablePreferences[PREFERENCES_KEY_TIME] = notificationTimeString
    }
}
