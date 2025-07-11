package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.mapper.preference.toDataModel
import com.websarva.wings.android.zuboradiary.data.mapper.preference.toDomainModel
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferenceFlowResult
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferences
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesException
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateCalendarStartDayOfWeekSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdatePassCodeSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateReminderNotificationSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateThemeColorSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateWeatherInfoFetchSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UserSettingsException
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingDataSourceResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class UserPreferencesRepository(private val userPreferences: UserPreferences) {

    fun fetchThemeColorPreference(): Flow<UserSettingDataSourceResult<ThemeColorSetting>> {
        return userPreferences.fetchThemeColorPreference()
            .map { result ->
                when (result) {
                    is UserPreferenceFlowResult.Success -> {
                        UserSettingDataSourceResult.Success(
                            result.preference.toDomainModel()
                        )
                    }

                    is UserPreferenceFlowResult.Failure -> {
                        UserSettingDataSourceResult.Failure(
                            mapPreferenceExceptionToSettingsException(result.exception)
                        )
                    }
                }
            }
    }

    fun fetchCalendarStartDayOfWeekPreference():
            Flow<UserSettingDataSourceResult<CalendarStartDayOfWeekSetting>> {
        return userPreferences.fetchCalendarStartDayOfWeekPreference()
            .map { result ->
                when (result) {
                    is UserPreferenceFlowResult.Success -> {
                        UserSettingDataSourceResult.Success(
                            result.preference.toDomainModel()
                        )
                    }

                    is UserPreferenceFlowResult.Failure -> {
                        UserSettingDataSourceResult.Failure(
                            mapPreferenceExceptionToSettingsException(result.exception)
                        )
                    }
                }
            }
    }

    fun fetchReminderNotificationPreference():
            Flow<UserSettingDataSourceResult<ReminderNotificationSetting>> {
        return userPreferences.fetchReminderNotificationPreference()
            .map { result ->
                when (result) {
                    is UserPreferenceFlowResult.Success -> {
                        UserSettingDataSourceResult.Success(
                            result.preference.toDomainModel()
                        )
                    }

                    is UserPreferenceFlowResult.Failure -> {
                        UserSettingDataSourceResult.Failure(
                            mapPreferenceExceptionToSettingsException(result.exception)
                        )
                    }
                }
            }
    }

    fun fetchPasscodeLockPreference():
            Flow<UserSettingDataSourceResult<PasscodeLockSetting>> {
        return userPreferences.fetchPasscodeLockPreference()
            .map { result ->
                when (result) {
                    is UserPreferenceFlowResult.Success -> {
                        UserSettingDataSourceResult.Success(
                            result.preference.toDomainModel()
                        )
                    }

                    is UserPreferenceFlowResult.Failure -> {
                        UserSettingDataSourceResult.Failure(
                            mapPreferenceExceptionToSettingsException(result.exception)
                        )
                    }
                }
            }
    }

    fun fetchWeatherInfoFetchPreference():
            Flow<UserSettingDataSourceResult<WeatherInfoFetchSetting>> {
        return userPreferences.fetchWeatherInfoFetchPreference()
            .map { result ->
                when (result) {
                    is UserPreferenceFlowResult.Success -> {
                        UserSettingDataSourceResult.Success(
                            WeatherInfoFetchSetting(result.preference.isEnabled)
                        )
                    }

                    is UserPreferenceFlowResult.Failure -> {
                        UserSettingDataSourceResult.Failure(
                            mapPreferenceExceptionToSettingsException(result.exception)
                        )
                    }
                }
            }
    }

    private fun mapPreferenceExceptionToSettingsException(
        preferenceException: UserPreferencesException
    ): UserSettingsException {
        return when (preferenceException) {
            is UserPreferencesException.DataStoreAccessFailed -> {
                UserSettingsException.AccessFailed(preferenceException)
            }
            is UserPreferencesException.DataNotFoundException -> {
                UserSettingsException.DataNotFoundException(preferenceException)
            }
        }
    }

    @Throws(UpdateThemeColorSettingFailedException::class)
    suspend fun saveThemeColorPreference(setting: ThemeColorSetting) {
        withContext(Dispatchers.IO) {
            try {
                val preference = setting.toDataModel()
                userPreferences.saveThemeColorPreference(preference)
            } catch (e: UserPreferencesException.DataStoreAccessFailed) {
                throw UpdateThemeColorSettingFailedException(setting.themeColor, e)
            }
        }
    }

    @Throws(UpdateCalendarStartDayOfWeekSettingFailedException::class)
    suspend fun saveCalendarStartDayOfWeekPreference(setting: CalendarStartDayOfWeekSetting) {
        withContext(Dispatchers.IO) {
            try {
                val preference = setting.toDataModel()
                userPreferences.saveCalendarStartDayOfWeekPreference(preference)
            } catch (e: UserPreferencesException.DataStoreAccessFailed) {
                throw UpdateCalendarStartDayOfWeekSettingFailedException(setting.dayOfWeek,e)
            }
        }
    }

    @Throws(UpdateReminderNotificationSettingFailedException::class)
    suspend fun saveReminderNotificationPreference(setting: ReminderNotificationSetting) {
        withContext(Dispatchers.IO) {
            try {
                val preference = setting.toDataModel()
                userPreferences.saveReminderNotificationPreference(preference)
            } catch (e: UserPreferencesException.DataStoreAccessFailed) {
                throw UpdateReminderNotificationSettingFailedException(
                    setting.isEnabled,
                    when (setting) {
                        is ReminderNotificationSetting.Enabled -> setting.notificationTime
                        ReminderNotificationSetting.Disabled -> null
                    },
                    e
                )
            }
        }
    }

    @Throws(UpdatePassCodeSettingFailedException::class)
    suspend fun savePasscodeLockPreference(setting: PasscodeLockSetting) {
        withContext(Dispatchers.IO) {
            try {
                val preference = setting.toDataModel()
                userPreferences.savePasscodeLockPreference(preference)
            } catch (e: UserPreferencesException.DataStoreAccessFailed) {
                throw UpdatePassCodeSettingFailedException(
                    setting.isEnabled,
                    when (setting) {
                        is PasscodeLockSetting.Enabled -> setting.passcode
                        PasscodeLockSetting.Disabled -> ""
                    },
                    e
                )
            }
        }
    }

    @Throws(UpdateWeatherInfoFetchSettingFailedException::class)
    suspend fun saveWeatherInfoFetchPreference(setting: WeatherInfoFetchSetting) {
        withContext(Dispatchers.IO) {
            try {
                val preference = setting.toDataModel()
                userPreferences.saveWeatherInfoFetchPreference(preference)
            } catch (e: UserPreferencesException.DataStoreAccessFailed) {
                throw UpdateWeatherInfoFetchSettingFailedException(setting.isEnabled, e)
            }
        }
    }
}
