package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.preferences.core.Preferences

class AllPreferences(preferences: Preferences) {

    val themeColorPreference: ThemeColorPreference = ThemeColorPreference(preferences)

    val calendarStartDayOfWeekPreference: CalendarStartDayOfWeekPreference =
        CalendarStartDayOfWeekPreference(preferences)

    val reminderNotificationPreference: ReminderNotificationPreference =
        ReminderNotificationPreference(preferences)

    val passcodeLockPreference: PassCodeLockPreference = PassCodeLockPreference(preferences)

    val weatherInfoAcquisitionPreference: WeatherInfoAcquisitionPreference =
        WeatherInfoAcquisitionPreference(preferences)
}
