package com.websarva.wings.android.zuboradiary.data.preferences

internal class ReminderNotificationPreference(
    val isEnabled: Boolean = false,
    val notificationTimeString: String = ""
) : UserPreference
