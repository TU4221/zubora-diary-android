package com.websarva.wings.android.zuboradiary.data.preferences

internal data class AllPreferences(
    val themeColorPreference: ThemeColorPreference,
    val calendarStartDayOfWeekPreference: CalendarStartDayOfWeekPreference,
    val reminderNotificationPreference: ReminderNotificationPreference,
    val passcodeLockPreference: PassCodeLockPreference,
    val weatherInfoAcquisitionPreference: WeatherInfoAcquisitionPreference,
)
