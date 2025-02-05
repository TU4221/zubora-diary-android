package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.preferences.core.Preferences
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

class UserPreferencesRepository(private val userPreferences: UserPreferences) {

    fun loadThemeColorPreference(): Flowable<ThemeColorPreference> {
        return userPreferences.loadThemeColorPreference()
    }

    fun saveThemeColorPreference(preference: ThemeColorPreference): Single<Preferences> {
        return userPreferences.saveThemeColorPreference(preference)
    }

    fun loadCalendarStartDayOfWeekPreference(): Flowable<CalendarStartDayOfWeekPreference> {
        return userPreferences.loadCalendarStartDayOfWeekPreference()
    }

    fun saveCalendarStartDayOfWeekPreference(preference: CalendarStartDayOfWeekPreference): Single<Preferences> {
        return userPreferences.saveCalendarStartDayOfWeekPreference(preference)
    }

    fun loadReminderNotificationPreference(): Flowable<ReminderNotificationPreference> {
        return userPreferences.loadReminderNotificationPreference()
    }

    fun saveReminderNotificationPreference(preference: ReminderNotificationPreference): Single<Preferences> {
        return userPreferences.saveReminderNotificationPreference(preference)
    }

    fun loadPasscodeLockPreference(): Flowable<PassCodeLockPreference> {
        return userPreferences.loadPasscodeLockPreference()
    }

    fun savePasscodeLockPreference(preference: PassCodeLockPreference): Single<Preferences> {
        return userPreferences.savePasscodeLockPreference(preference)
    }

    fun loadWeatherInfoAcquisitionPreference(): Flowable<WeatherInfoAcquisitionPreference> {
        return userPreferences.loadWeatherInfoAcquisitionPreference()
    }

    fun saveWeatherInfoAcquisitionPreference(preference: WeatherInfoAcquisitionPreference): Single<Preferences> {
        return userPreferences.saveWeatherInfoAcquisitionPreference(preference)
    }

    fun initializeAllPreferences(): Single<Preferences> {
        return userPreferences.initializeAllPreferences()
    }
}
