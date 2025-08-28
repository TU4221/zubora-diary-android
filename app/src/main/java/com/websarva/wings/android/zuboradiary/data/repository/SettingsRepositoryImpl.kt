package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.mapper.preference.toDataModel
import com.websarva.wings.android.zuboradiary.data.mapper.preference.toDomainModel
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferenceFlowResult
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesDataSource
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesException
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.domain.exception.settings.CalendarStartDayOfWeekSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.PassCodeSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.ReminderNotificationSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.ThemeColorSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.WeatherInfoFetchSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UserSettingsException
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingDataSourceResult
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class SettingsRepositoryImpl(
    private val userPreferencesDataSource: UserPreferencesDataSource
) : SettingsRepository {

    override fun loadThemeColorPreference(): Flow<UserSettingDataSourceResult<ThemeColorSetting>> {
        return userPreferencesDataSource.loadThemeColorPreference()
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

    override fun loadCalendarStartDayOfWeekPreference():
            Flow<UserSettingDataSourceResult<CalendarStartDayOfWeekSetting>> {
        return userPreferencesDataSource.loadCalendarStartDayOfWeekPreference()
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

    override fun loadReminderNotificationPreference():
            Flow<UserSettingDataSourceResult<ReminderNotificationSetting>> {
        return userPreferencesDataSource.loadReminderNotificationPreference()
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

    override fun loadPasscodeLockPreference():
            Flow<UserSettingDataSourceResult<PasscodeLockSetting>> {
        return userPreferencesDataSource.loadPasscodeLockPreference()
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

    override fun loadWeatherInfoFetchPreference():
            Flow<UserSettingDataSourceResult<WeatherInfoFetchSetting>> {
        return userPreferencesDataSource.loadWeatherInfoFetchPreference()
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

    @Throws(ThemeColorSettingUpdateFailureException::class)
    override suspend fun saveThemeColorPreference(setting: ThemeColorSetting) {
        withContext(Dispatchers.IO) {
            try {
                val preference = setting.toDataModel()
                userPreferencesDataSource.saveThemeColorPreference(preference)
            } catch (e: UserPreferencesException.DataStoreAccessFailure) {
                throw ThemeColorSettingUpdateFailureException(setting.themeColor, e)
            }
        }
    }

    @Throws(CalendarStartDayOfWeekSettingUpdateFailureException::class)
    override suspend fun saveCalendarStartDayOfWeekPreference(setting: CalendarStartDayOfWeekSetting) {
        withContext(Dispatchers.IO) {
            try {
                val preference = setting.toDataModel()
                userPreferencesDataSource.saveCalendarStartDayOfWeekPreference(preference)
            } catch (e: UserPreferencesException.DataStoreAccessFailure) {
                throw CalendarStartDayOfWeekSettingUpdateFailureException(setting.dayOfWeek,e)
            }
        }
    }

    @Throws(ReminderNotificationSettingUpdateFailureException::class)
    override suspend fun saveReminderNotificationPreference(setting: ReminderNotificationSetting) {
        withContext(Dispatchers.IO) {
            try {
                val preference = setting.toDataModel()
                userPreferencesDataSource.saveReminderNotificationPreference(preference)
            } catch (e: UserPreferencesException.DataStoreAccessFailure) {
                throw ReminderNotificationSettingUpdateFailureException(
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

    @Throws(PassCodeSettingUpdateFailureException::class)
    override suspend fun savePasscodeLockPreference(setting: PasscodeLockSetting) {
        withContext(Dispatchers.IO) {
            try {
                val preference = setting.toDataModel()
                userPreferencesDataSource.savePasscodeLockPreference(preference)
            } catch (e: UserPreferencesException.DataStoreAccessFailure) {
                throw PassCodeSettingUpdateFailureException(
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

    @Throws(WeatherInfoFetchSettingUpdateFailureException::class)
    override suspend fun saveWeatherInfoFetchPreference(setting: WeatherInfoFetchSetting) {
        withContext(Dispatchers.IO) {
            try {
                val preference = setting.toDataModel()
                userPreferencesDataSource.saveWeatherInfoFetchPreference(preference)
            } catch (e: UserPreferencesException.DataStoreAccessFailure) {
                throw WeatherInfoFetchSettingUpdateFailureException(setting.isEnabled, e)
            }
        }
    }

    private fun mapPreferenceExceptionToSettingsException(
        preferenceException: UserPreferencesException
    ): UserSettingsException {
        return when (preferenceException) {
            is UserPreferencesException.DataStoreAccessFailure -> {
                UserSettingsException.AccessFailure(preferenceException)
            }
            is UserPreferencesException.DataNotFound -> {
                UserSettingsException.DataNotFound(preferenceException)
            }
        }
    }
}
