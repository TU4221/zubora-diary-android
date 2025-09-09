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
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.NotFoundException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.RepositoryException
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

                throw mapPreferenceExceptionToRepositoryException(cause)
            }
    }

    override fun loadCalendarStartDayOfWeekPreference(): Flow<CalendarStartDayOfWeekSetting> {
        return userPreferencesDataSource.loadCalendarStartDayOfWeekPreference()
            .map { preference ->
                preference.toDomainModel()
            }.catch { cause ->
                if (cause !is UserPreferencesException) return@catch

                throw mapPreferenceExceptionToRepositoryException(cause)
            }
    }

    override fun loadReminderNotificationPreference(): Flow<ReminderNotificationSetting> {
        return userPreferencesDataSource.loadReminderNotificationPreference()
            .map { preference ->
                preference.toDomainModel()
            }.catch { cause ->
                if (cause !is UserPreferencesException) return@catch

                throw mapPreferenceExceptionToRepositoryException(cause)
            }
    }

    override fun loadPasscodeLockPreference(): Flow<PasscodeLockSetting> {
        return userPreferencesDataSource.loadPasscodeLockPreference()
            .map { preference ->
                preference.toDomainModel()
            }.catch { cause ->
                if (cause !is UserPreferencesException) return@catch

                throw mapPreferenceExceptionToRepositoryException(cause)
            }
    }

    override fun loadWeatherInfoFetchPreference(): Flow<WeatherInfoFetchSetting> {
        return userPreferencesDataSource.loadWeatherInfoFetchPreference()
            .map { preference ->
                preference.toDomainModel()
            }.catch { cause ->
                if (cause !is UserPreferencesException) return@catch

                throw mapPreferenceExceptionToRepositoryException(cause)
            }
    }

    override suspend fun saveThemeColorPreference(setting: ThemeColorSetting) {
        withContext(Dispatchers.IO) {
            try {
                val preference = setting.toDataModel()
                userPreferencesDataSource.saveThemeColorPreference(preference)
            } catch (e: UserPreferencesException.DataStoreAccessFailure) {
                throw DataStorageException(cause = e)
            }
        }
    }

    override suspend fun saveCalendarStartDayOfWeekPreference(setting: CalendarStartDayOfWeekSetting) {
        withContext(Dispatchers.IO) {
            try {
                val preference = setting.toDataModel()
                userPreferencesDataSource.saveCalendarStartDayOfWeekPreference(preference)
            } catch (e: UserPreferencesException.DataStoreAccessFailure) {
                throw DataStorageException(cause = e)
            }
        }
    }

    override suspend fun saveReminderNotificationPreference(setting: ReminderNotificationSetting) {
        withContext(Dispatchers.IO) {
            try {
                val preference = setting.toDataModel()
                userPreferencesDataSource.saveReminderNotificationPreference(preference)
            } catch (e: UserPreferencesException.DataStoreAccessFailure) {
                throw DataStorageException(cause = e)
            }
        }
    }

    override suspend fun savePasscodeLockPreference(setting: PasscodeLockSetting) {
        withContext(Dispatchers.IO) {
            try {
                val preference = setting.toDataModel()
                userPreferencesDataSource.savePasscodeLockPreference(preference)
            } catch (e: UserPreferencesException.DataStoreAccessFailure) {
                throw DataStorageException(cause = e)
            }
        }
    }

    override suspend fun saveWeatherInfoFetchPreference(setting: WeatherInfoFetchSetting) {
        withContext(Dispatchers.IO) {
            try {
                val preference = setting.toDataModel()
                userPreferencesDataSource.saveWeatherInfoFetchPreference(preference)
            } catch (e: UserPreferencesException.DataStoreAccessFailure) {
                throw DataStorageException(cause = e)
            }
        }
    }

    private fun mapPreferenceExceptionToRepositoryException(
        preferenceException: UserPreferencesException
    ): RepositoryException {
        return when (preferenceException) {
            is UserPreferencesException.DataStoreAccessFailure -> {
                DataStorageException(cause = preferenceException)
            }
            is UserPreferencesException.DataNotFound -> {
                NotFoundException()
            }
        }
    }
}
