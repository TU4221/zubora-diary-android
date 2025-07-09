package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.preferences.CalendarStartDayOfWeekPreference
import com.websarva.wings.android.zuboradiary.data.preferences.PassCodeLockPreference
import com.websarva.wings.android.zuboradiary.data.preferences.ReminderNotificationPreference
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColorPreference
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferenceFlowResult
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferences
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesAccessException
import com.websarva.wings.android.zuboradiary.data.preferences.WeatherInfoFetchPreference
import com.websarva.wings.android.zuboradiary.domain.exception.settings.InitializeSettingsFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateCalendarStartDayOfWeekSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdatePassCodeSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateReminderNotificationSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateThemeColorSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateWeatherInfoFetchSettingFailedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

internal class UserPreferencesRepository(private val userPreferences: UserPreferences) {

    fun fetchThemeColorPreference(): Flow<UserPreferenceFlowResult<ThemeColorPreference>> {
        return userPreferences.fetchThemeColorPreference()
    }

    fun fetchCalendarStartDayOfWeekPreference():
            Flow<UserPreferenceFlowResult<CalendarStartDayOfWeekPreference>> {
        return userPreferences.fetchCalendarStartDayOfWeekPreference()
    }

    fun fetchReminderNotificationPreference():
            Flow<UserPreferenceFlowResult<ReminderNotificationPreference>> {
        return userPreferences.fetchReminderNotificationPreference()
    }

    fun fetchPasscodeLockPreference():
            Flow<UserPreferenceFlowResult<PassCodeLockPreference>> {
        return userPreferences.fetchPasscodeLockPreference()
    }

    fun fetchWeatherInfoFetchPreference():
            Flow<UserPreferenceFlowResult<WeatherInfoFetchPreference>> {
        return userPreferences.fetchWeatherInfoFetchPreference()
    }

    @Throws(UpdateThemeColorSettingFailedException::class)
    suspend fun saveThemeColorPreference(preference: ThemeColorPreference) {
        withContext(Dispatchers.IO) {
            try {
                userPreferences.saveThemeColorPreference(preference)
            } catch (e: UserPreferencesAccessException) {
                throw UpdateThemeColorSettingFailedException(preference.themeColor, e)
            }
        }
    }

    @Throws(UpdateCalendarStartDayOfWeekSettingFailedException::class)
    suspend fun saveCalendarStartDayOfWeekPreference(preference: CalendarStartDayOfWeekPreference) {
        withContext(Dispatchers.IO) {
            try {
                userPreferences.saveCalendarStartDayOfWeekPreference(preference)
            } catch (e: UserPreferencesAccessException) {
                throw UpdateCalendarStartDayOfWeekSettingFailedException(preference.dayOfWeek,e)
            }
        }
    }

    @Throws(UpdateReminderNotificationSettingFailedException::class)
    suspend fun saveReminderNotificationPreference(preference: ReminderNotificationPreference) {
        withContext(Dispatchers.IO) {
            try {
                userPreferences.saveReminderNotificationPreference(preference)
            } catch (e: UserPreferencesAccessException) {
                throw UpdateReminderNotificationSettingFailedException(
                    preference.isChecked,
                    preference.notificationLocalTime,
                    e
                )
            }
        }
    }

    @Throws(UpdatePassCodeSettingFailedException::class)
    suspend fun savePasscodeLockPreference(preference: PassCodeLockPreference) {
        withContext(Dispatchers.IO) {
            try {
                userPreferences.savePasscodeLockPreference(preference)
            } catch (e: UserPreferencesAccessException) {
                throw UpdatePassCodeSettingFailedException(
                    preference.isChecked,
                    preference.passCode,
                    e
                )
            }
        }
    }

    @Throws(UpdateWeatherInfoFetchSettingFailedException::class)
    suspend fun saveWeatherInfoFetchPreference(preference: WeatherInfoFetchPreference) {
        withContext(Dispatchers.IO) {
            try {
                userPreferences.saveWeatherInfoFetchPreference(preference)
            } catch (e: UserPreferencesAccessException) {
                throw UpdateWeatherInfoFetchSettingFailedException(preference.isChecked, e)
            }
        }
    }

    @Throws(InitializeSettingsFailedException::class)
    suspend fun initializeAllPreferences() {
        withContext(Dispatchers.IO) {
            try {
                userPreferences.initializeAllPreferences()
            } catch (e: UserPreferencesAccessException) {
                throw InitializeSettingsFailedException(e)
            }
        }
    }
}
