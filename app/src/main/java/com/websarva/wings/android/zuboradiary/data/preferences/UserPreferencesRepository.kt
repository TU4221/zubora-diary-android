package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.core.IOException
import kotlinx.coroutines.flow.Flow

class UserPreferencesRepository(private val userPreferences: UserPreferences) {

    @Throws(Throwable::class)
    fun loadThemeColorPreference(): Flow<ThemeColorPreference> {
        return userPreferences.loadThemeColorPreference()
    }

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun saveThemeColorPreference(preference: ThemeColorPreference) {
        userPreferences.saveThemeColorPreference(preference)
    }

    @Throws(Throwable::class)
    fun loadCalendarStartDayOfWeekPreference(): Flow<CalendarStartDayOfWeekPreference> {
        return userPreferences.loadCalendarStartDayOfWeekPreference()
    }

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun saveCalendarStartDayOfWeekPreference(preference: CalendarStartDayOfWeekPreference) {
        return userPreferences.saveCalendarStartDayOfWeekPreference(preference)
    }

    @Throws(Throwable::class)
    fun loadReminderNotificationPreference(): Flow<ReminderNotificationPreference> {
        return userPreferences.loadReminderNotificationPreference()
    }

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun saveReminderNotificationPreference(preference: ReminderNotificationPreference) {
        return userPreferences.saveReminderNotificationPreference(preference)
    }

    @Throws(Throwable::class)
    fun loadPasscodeLockPreference(): Flow<PassCodeLockPreference> {
        return userPreferences.loadPasscodeLockPreference()
    }

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun savePasscodeLockPreference(preference: PassCodeLockPreference) {
        return userPreferences.savePasscodeLockPreference(preference)
    }

    @Throws(Throwable::class)
    fun loadWeatherInfoAcquisitionPreference(): Flow<WeatherInfoAcquisitionPreference> {
        return userPreferences.loadWeatherInfoAcquisitionPreference()
    }

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun saveWeatherInfoAcquisitionPreference(preference: WeatherInfoAcquisitionPreference) {
        return userPreferences.saveWeatherInfoAcquisitionPreference(preference)
    }

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun initializeAllPreferences() {
        return userPreferences.initializeAllPreferences()
    }
}
