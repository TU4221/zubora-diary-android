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
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

internal class SettingsRepositoryImpl(
    private val userPreferencesDataSource: UserPreferencesDataSource
) : SettingsRepository {

    override fun loadThemeColorSetting(): Flow<ThemeColorSetting> {
        return userPreferencesDataSource.loadThemeColorPreference()
            .map { preference ->
                preference.toDomainModel()
            }.catch { cause ->
                if (cause !is UserPreferencesException) return@catch

                throw mapPreferenceExceptionToRepositoryException(cause)
            }
    }

    override fun loadCalendarStartDayOfWeekSetting(): Flow<CalendarStartDayOfWeekSetting> {
        return userPreferencesDataSource.loadCalendarStartDayOfWeekPreference()
            .map { preference ->
                preference.toDomainModel()
            }.catch { cause ->
                if (cause !is UserPreferencesException) return@catch

                throw mapPreferenceExceptionToRepositoryException(cause)
            }
    }

    override fun loadReminderNotificationSetting(): Flow<ReminderNotificationSetting> {
        return userPreferencesDataSource.loadReminderNotificationPreference()
            .map { preference ->
                preference.toDomainModel()
            }.catch { cause ->
                if (cause !is UserPreferencesException) return@catch

                throw mapPreferenceExceptionToRepositoryException(cause)
            }
    }

    override fun loadPasscodeLockSetting(): Flow<PasscodeLockSetting> {
        return userPreferencesDataSource.loadPasscodeLockPreference()
            .map { preference ->
                preference.toDomainModel()
            }.catch { cause ->
                if (cause !is UserPreferencesException) return@catch

                throw mapPreferenceExceptionToRepositoryException(cause)
            }
    }

    override fun loadWeatherInfoFetchSetting(): Flow<WeatherInfoFetchSetting> {
        return userPreferencesDataSource.loadWeatherInfoFetchPreference()
            .map { preference ->
                preference.toDomainModel()
            }.catch { cause ->
                if (cause !is UserPreferencesException) return@catch

                throw mapPreferenceExceptionToRepositoryException(cause)
            }
    }

    override suspend fun updateThemeColorSetting(setting: ThemeColorSetting) {
        try {
            val preference = setting.toDataModel()
            userPreferencesDataSource.updateThemeColorPreference(preference)
        } catch (e: UserPreferencesException.DataStoreAccessFailure) {
            throw DataStorageException(cause = e)
        }
    }

    override suspend fun updateCalendarStartDayOfWeekSetting(setting: CalendarStartDayOfWeekSetting) {
        try {
            val preference = setting.toDataModel()
            userPreferencesDataSource.updateCalendarStartDayOfWeekPreference(preference)
        } catch (e: UserPreferencesException.DataStoreAccessFailure) {
            throw DataStorageException(cause = e)
        }
    }

    override suspend fun updateReminderNotificationSetting(setting: ReminderNotificationSetting) {
        try {
            val preference = setting.toDataModel()
            userPreferencesDataSource.updateReminderNotificationPreference(preference)
        } catch (e: UserPreferencesException.DataStoreAccessFailure) {
            throw DataStorageException(cause = e)
        }
    }

    override suspend fun updatePasscodeLockSetting(setting: PasscodeLockSetting) {
        try {
            val preference = setting.toDataModel()
            userPreferencesDataSource.updatePasscodeLockPreference(preference)
        } catch (e: UserPreferencesException.DataStoreAccessFailure) {
            throw DataStorageException(cause = e)
        }
    }

    override suspend fun updateWeatherInfoFetchSetting(setting: WeatherInfoFetchSetting) {
        try {
            val preference = setting.toDataModel()
            userPreferencesDataSource.updateWeatherInfoFetchPreference(preference)
        } catch (e: UserPreferencesException.DataStoreAccessFailure) {
            throw DataStorageException(cause = e)
        }
    }

    private fun mapPreferenceExceptionToRepositoryException(
        preferenceException: UserPreferencesException
    ): DomainException {
        return when (preferenceException) {
            is UserPreferencesException.DataStoreAccessFailure -> {
                DataStorageException(cause = preferenceException)
            }
            is UserPreferencesException.DataNotFound -> {
                ResourceNotFoundException()
            }
        }
    }
}
