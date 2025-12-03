package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.mapper.settings.SettingsRepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.mapper.settings.toDataModel
import com.websarva.wings.android.zuboradiary.data.mapper.settings.toDomainModel
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesDataSource
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class SettingsRepositoryImpl @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource
) : SettingsRepository {

    override fun loadThemeColorSetting(): Flow<ThemeColorSetting?> {
        return userPreferencesDataSource.loadThemeColorPreference()
            .map { preference ->
                preference?.toDomainModel()
            }.catch { cause ->
                throw if (cause is Exception) {
                    SettingsRepositoryExceptionMapper.toDomainException(cause)
                } else {
                    cause
                }
            }
    }

    override fun loadCalendarStartDayOfWeekSetting(): Flow<CalendarStartDayOfWeekSetting?> {
        return userPreferencesDataSource.loadCalendarStartDayOfWeekPreference()
            .map { preference ->
                preference?.toDomainModel()
            }.catch { cause ->
                throw if (cause is Exception) {
                    SettingsRepositoryExceptionMapper.toDomainException(cause)
                } else {
                    cause
                }
            }
    }

    override fun loadReminderNotificationSetting(): Flow<ReminderNotificationSetting?> {
        return userPreferencesDataSource.loadReminderNotificationPreference()
            .map { preference ->
                preference?.toDomainModel()
            }.catch { cause ->
                throw if (cause is Exception) {
                    SettingsRepositoryExceptionMapper.toDomainException(cause)
                } else {
                    cause
                }
            }
    }

    override fun loadPasscodeLockSetting(): Flow<PasscodeLockSetting?> {
        return userPreferencesDataSource.loadPasscodeLockPreference()
            .map { preference ->
                preference?.toDomainModel()
            }.catch { cause ->
                throw if (cause is Exception) {
                    SettingsRepositoryExceptionMapper.toDomainException(cause)
                } else {
                    cause
                }
            }
    }

    override fun loadWeatherInfoFetchSetting(): Flow<WeatherInfoFetchSetting?> {
        return userPreferencesDataSource.loadWeatherInfoFetchPreference()
            .map { preference ->
                preference?.toDomainModel()
            }.catch { cause ->
                throw if (cause is Exception) {
                    SettingsRepositoryExceptionMapper.toDomainException(cause)
                } else {
                    cause
                }
            }
    }

    override suspend fun updateThemeColorSetting(setting: ThemeColorSetting) {
        try {
            val preference = setting.toDataModel()
            userPreferencesDataSource.updateThemeColorPreference(preference)
        } catch (e: Exception) {
            throw SettingsRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun updateCalendarStartDayOfWeekSetting(setting: CalendarStartDayOfWeekSetting) {
        try {
            val preference = setting.toDataModel()
            userPreferencesDataSource.updateCalendarStartDayOfWeekPreference(preference)
        } catch (e: Exception) {
            throw SettingsRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun updateReminderNotificationSetting(setting: ReminderNotificationSetting) {
        try {
            val preference = setting.toDataModel()
            userPreferencesDataSource.updateReminderNotificationPreference(preference)
        } catch (e: Exception) {
            throw SettingsRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun updatePasscodeLockSetting(setting: PasscodeLockSetting) {
        try {
            val preference = setting.toDataModel()
            userPreferencesDataSource.updatePasscodeLockPreference(preference)
        } catch (e: Exception) {
            throw SettingsRepositoryExceptionMapper.toDomainException(e)
        }
    }

    override suspend fun updateWeatherInfoFetchSetting(setting: WeatherInfoFetchSetting) {
        try {
            val preference = setting.toDataModel()
            userPreferencesDataSource.updateWeatherInfoFetchPreference(preference)
        } catch (e: Exception) {
            throw SettingsRepositoryExceptionMapper.toDomainException(e)
        }
    }
}
