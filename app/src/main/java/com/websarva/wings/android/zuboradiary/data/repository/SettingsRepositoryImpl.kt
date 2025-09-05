package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.mapper.preference.toDataModel
import com.websarva.wings.android.zuboradiary.data.mapper.preference.toDomainModel
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesDataSource
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesException
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.domain.exception.settings.CalendarStartDayOfWeekSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.PassCodeSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.ReminderNotificationSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.ThemeColorSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.WeatherInfoFetchSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UserSettingsLoadException
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class SettingsRepositoryImpl(
    private val userPreferencesDataSource: UserPreferencesDataSource
) : SettingsRepository {

    override fun loadThemeColorPreference(): Flow<ThemeColorSetting> {
        return userPreferencesDataSource.loadThemeColorPreference()
            .map { preference ->
                preference.toDomainModel()
            }.catch { cause ->
                if (cause !is UserPreferencesException) return@catch

                throw mapPreferenceExceptionToSettingsException(cause)
            }
    }

    override fun loadCalendarStartDayOfWeekPreference(): Flow<CalendarStartDayOfWeekSetting> {
        return userPreferencesDataSource.loadCalendarStartDayOfWeekPreference()
            .map { preference ->
                preference.toDomainModel()
            }.catch { cause ->
                if (cause !is UserPreferencesException) return@catch

                throw mapPreferenceExceptionToSettingsException(cause)
            }
    }

    override fun loadReminderNotificationPreference(): Flow<ReminderNotificationSetting> {
        return userPreferencesDataSource.loadReminderNotificationPreference()
            .map { preference ->
                preference.toDomainModel()
            }.catch { cause ->
                if (cause !is UserPreferencesException) return@catch

                throw mapPreferenceExceptionToSettingsException(cause)
            }
    }

    override fun loadPasscodeLockPreference(): Flow<PasscodeLockSetting> {
        return userPreferencesDataSource.loadPasscodeLockPreference()
            .map { preference ->
                preference.toDomainModel()
            }.catch { cause ->
                if (cause !is UserPreferencesException) return@catch

                throw mapPreferenceExceptionToSettingsException(cause)
            }
    }

    override fun loadWeatherInfoFetchPreference(): Flow<WeatherInfoFetchSetting> {
        return userPreferencesDataSource.loadWeatherInfoFetchPreference()
            .map { preference ->
                preference.toDomainModel()
            }.catch { cause ->
                if (cause !is UserPreferencesException) return@catch

                throw mapPreferenceExceptionToSettingsException(cause)
            }
    }

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
    ): UserSettingsLoadException {
        return when (preferenceException) {
            is UserPreferencesException.DataStoreAccessFailure -> {
                UserSettingsLoadException.AccessFailure(preferenceException)
            }
            is UserPreferencesException.DataNotFound -> {
                UserSettingsLoadException.DataNotFound(preferenceException)
            }
        }
    }
}
