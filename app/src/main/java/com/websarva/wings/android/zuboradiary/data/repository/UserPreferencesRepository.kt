package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.preferences.AllPreferences
import com.websarva.wings.android.zuboradiary.data.preferences.CalendarStartDayOfWeekPreference
import com.websarva.wings.android.zuboradiary.data.preferences.PassCodeLockPreference
import com.websarva.wings.android.zuboradiary.data.preferences.ReminderNotificationPreference
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColorPreference
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferences
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesAccessException
import com.websarva.wings.android.zuboradiary.data.preferences.WeatherInfoAcquisitionPreference
import com.websarva.wings.android.zuboradiary.domain.model.error.UserSettingsError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

internal class UserPreferencesRepository(private val userPreferences: UserPreferences) {

    fun loadAllPreferences(): Flow<AllPreferences> {
        return userPreferences.loadAllPreferences()
    }

    @Throws(UserSettingsError.UpdateSettings::class)
    private suspend fun executePreferenceUpdate(
        updateOperation: suspend () -> Unit // 実行したい更新処理をラムダとして受け取る
    ){
        return try {
            withContext(Dispatchers.IO) {
                updateOperation()
            }
        } catch (e: UserPreferencesAccessException) {
            throw UserSettingsError.UpdateSettings(e)
        }
    }

    @Throws(UserSettingsError.UpdateSettings::class)
    suspend fun saveThemeColorPreference(preference: ThemeColorPreference) {
        executePreferenceUpdate {
            userPreferences.saveThemeColorPreference(preference)
        }
    }

    @Throws(UserSettingsError.UpdateSettings::class)
    suspend fun saveCalendarStartDayOfWeekPreference(preference: CalendarStartDayOfWeekPreference) {
        executePreferenceUpdate {
            userPreferences.saveCalendarStartDayOfWeekPreference(preference)
        }
    }

    @Throws(UserSettingsError.UpdateSettings::class)
    suspend fun saveReminderNotificationPreference(preference: ReminderNotificationPreference) {
        executePreferenceUpdate {
            userPreferences.saveReminderNotificationPreference(preference)
        }
    }

    @Throws(UserSettingsError.UpdateSettings::class)
    suspend fun savePasscodeLockPreference(preference: PassCodeLockPreference) {
        executePreferenceUpdate {
            userPreferences.savePasscodeLockPreference(preference)
        }
    }

    @Throws(UserSettingsError.UpdateSettings::class)
    suspend fun saveWeatherInfoAcquisitionPreference(preference: WeatherInfoAcquisitionPreference) {
        executePreferenceUpdate {
            userPreferences.saveWeatherInfoAcquisitionPreference(preference)
        }
    }

    @Throws(UserSettingsError.UpdateSettings::class)
    suspend fun initializeAllPreferences() {
        executePreferenceUpdate {
            userPreferences.initializeAllPreferences()
        }
    }
}
