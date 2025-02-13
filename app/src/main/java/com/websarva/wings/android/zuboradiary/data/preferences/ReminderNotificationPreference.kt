package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.time.LocalTime

class ReminderNotificationPreference {

    companion object {
        private const val IS_CHECKED_DEFAULT_VALUE = false
        private const val NOTIFICATION_TIME_DEFAULT_VALUE = ""
    }

    private val isCheckedPreferenceKey =
        booleanPreferencesKey("is_checked_reminder_notification")
    private val notificationTimePreferenceKey =
        stringPreferencesKey("reminder_notification_time")

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

    constructor(preferences: Preferences) {
        var isChecked = preferences[isCheckedPreferenceKey]
        var notificationTimeString = preferences[notificationTimePreferenceKey]
        if (isChecked == null || notificationTimeString == null) {
            isChecked = IS_CHECKED_DEFAULT_VALUE
            notificationTimeString = NOTIFICATION_TIME_DEFAULT_VALUE
        }
        this.isChecked = isChecked
        this.notificationTimeString = notificationTimeString
    }

    @JvmOverloads
    constructor(
        isChecked: Boolean = IS_CHECKED_DEFAULT_VALUE,
        notificationTime: String = NOTIFICATION_TIME_DEFAULT_VALUE
    ) {
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
        mutablePreferences[isCheckedPreferenceKey] = isChecked
        mutablePreferences[notificationTimePreferenceKey] = notificationTimeString
    }
}
