package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.preferences.AllPreferences
import com.websarva.wings.android.zuboradiary.data.preferences.CalendarStartDayOfWeekPreference
import com.websarva.wings.android.zuboradiary.data.preferences.PassCodeLockPreference
import com.websarva.wings.android.zuboradiary.data.preferences.ReminderNotificationPreference
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColorPreference
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferences
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesAccessException
import com.websarva.wings.android.zuboradiary.data.preferences.WeatherInfoAcquisitionPreference
import com.websarva.wings.android.zuboradiary.domain.exception.settings.InitializeSettingsFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateCalendarStartDayOfWeekSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdatePassCodeSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateReminderNotificationSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateThemeColorSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateWeatherInfoAcquisitionSettingFailedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

internal class UserPreferencesRepository(private val userPreferences: UserPreferences) {

    fun loadAllPreferences(): Flow<AllPreferences> {
        return userPreferences.loadAllPreferences()
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

    @Throws(UpdateWeatherInfoAcquisitionSettingFailedException::class)
    suspend fun saveWeatherInfoAcquisitionPreference(preference: WeatherInfoAcquisitionPreference) {
        withContext(Dispatchers.IO) {
            try {
                userPreferences.saveWeatherInfoAcquisitionPreference(preference)
            } catch (e: UserPreferencesAccessException) {
                throw UpdateWeatherInfoAcquisitionSettingFailedException(preference.isChecked, e)
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
