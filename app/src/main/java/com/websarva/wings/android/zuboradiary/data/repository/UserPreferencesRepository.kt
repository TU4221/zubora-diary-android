package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.mapper.preference.toDataModel
import com.websarva.wings.android.zuboradiary.data.mapper.preference.toDomainModel
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferenceFlowResult
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferences
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesAccessException
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.domain.exception.settings.InitializeSettingsFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateCalendarStartDayOfWeekSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdatePassCodeSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateReminderNotificationSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateThemeColorSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateWeatherInfoFetchSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UserSettingsAccessException
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingFlowResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class UserPreferencesRepository(private val userPreferences: UserPreferences) {

    fun fetchThemeColorPreference(): Flow<UserSettingFlowResult<ThemeColorSetting>> {
        return userPreferences.fetchThemeColorPreference()
            .map { result ->
                when (result) {
                    is UserPreferenceFlowResult.Success -> {
                        UserSettingFlowResult.Success(
                            result.preference.toDomainModel()
                        )
                    }

                    is UserPreferenceFlowResult.Failure -> {
                        UserSettingFlowResult.Failure(
                            UserSettingsAccessException(result.exception),
                            result.fallbackPreference.toDomainModel()
                        )
                    }
                }
            }
    }

    fun fetchCalendarStartDayOfWeekPreference():
            Flow<UserSettingFlowResult<CalendarStartDayOfWeekSetting>> {
        return userPreferences.fetchCalendarStartDayOfWeekPreference()
            .map { result ->
                when (result) {
                    is UserPreferenceFlowResult.Success -> {
                        UserSettingFlowResult.Success(
                            result.preference.toDomainModel()
                        )
                    }

                    is UserPreferenceFlowResult.Failure -> {
                        UserSettingFlowResult.Failure(
                            UserSettingsAccessException(result.exception),
                            result.fallbackPreference.toDomainModel()
                        )
                    }
                }
            }
    }

    fun fetchReminderNotificationPreference():
            Flow<UserSettingFlowResult<ReminderNotificationSetting>> {
        return userPreferences.fetchReminderNotificationPreference()
            .map { result ->
                when (result) {
                    is UserPreferenceFlowResult.Success -> {
                        UserSettingFlowResult.Success(
                            result.preference.toDomainModel()
                        )
                    }

                    is UserPreferenceFlowResult.Failure -> {
                        UserSettingFlowResult.Failure(
                            UserSettingsAccessException(result.exception),
                            result.fallbackPreference.toDomainModel()
                        )
                    }
                }
            }
    }

    fun fetchPasscodeLockPreference():
            Flow<UserSettingFlowResult<PasscodeLockSetting>> {
        return userPreferences.fetchPasscodeLockPreference()
            .map { result ->
                when (result) {
                    is UserPreferenceFlowResult.Success -> {
                        UserSettingFlowResult.Success(
                            result.preference.toDomainModel()
                        )
                    }

                    is UserPreferenceFlowResult.Failure -> {
                        UserSettingFlowResult.Failure(
                            UserSettingsAccessException(result.exception),
                            result.fallbackPreference.toDomainModel()
                        )
                    }
                }
            }
    }

    fun fetchWeatherInfoFetchPreference():
            Flow<UserSettingFlowResult<WeatherInfoFetchSetting>> {
        return userPreferences.fetchWeatherInfoFetchPreference()
            .map { result ->
                when (result) {
                    is UserPreferenceFlowResult.Success -> {
                        UserSettingFlowResult.Success(
                            WeatherInfoFetchSetting(result.preference.isChecked)
                        )
                    }

                    is UserPreferenceFlowResult.Failure -> {
                        UserSettingFlowResult.Failure(
                            UserSettingsAccessException(result.exception),
                            WeatherInfoFetchSetting(result.fallbackPreference.isChecked)
                        )
                    }
                }
            }
    }

    @Throws(UpdateThemeColorSettingFailedException::class)
    suspend fun saveThemeColorPreference(setting: ThemeColorSetting) {
        withContext(Dispatchers.IO) {
            try {
                val preference = setting.toDataModel()
                userPreferences.saveThemeColorPreference(preference)
            } catch (e: UserPreferencesAccessException) {
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
            } catch (e: UserPreferencesAccessException) {
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
            } catch (e: UserPreferencesAccessException) {
                throw UpdateReminderNotificationSettingFailedException(
                    setting.isChecked,
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
            } catch (e: UserPreferencesAccessException) {
                throw UpdatePassCodeSettingFailedException(
                    setting.isChecked,
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
            } catch (e: UserPreferencesAccessException) {
                throw UpdateWeatherInfoFetchSettingFailedException(setting.isChecked, e)
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
