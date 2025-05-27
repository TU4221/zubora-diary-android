package com.websarva.wings.android.zuboradiary.data.repository

import androidx.datastore.core.IOException
import com.websarva.wings.android.zuboradiary.data.preferences.AllPreferences
import com.websarva.wings.android.zuboradiary.data.preferences.CalendarStartDayOfWeekPreference
import com.websarva.wings.android.zuboradiary.data.preferences.PassCodeLockPreference
import com.websarva.wings.android.zuboradiary.data.preferences.ReminderNotificationPreference
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColorPreference
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferences
import com.websarva.wings.android.zuboradiary.data.preferences.WeatherInfoAcquisitionPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

internal class UserPreferencesRepository(private val userPreferences: UserPreferences) {

    @Throws(Throwable::class)
    fun loadAllPreferences(): Flow<AllPreferences> {
        return userPreferences.loadAllPreferences()
    }

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun saveThemeColorPreference(preference: ThemeColorPreference) {
        withContext(Dispatchers.IO) {
            userPreferences.saveThemeColorPreference(preference)
        }
    }

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun saveCalendarStartDayOfWeekPreference(preference: CalendarStartDayOfWeekPreference) {
        withContext(Dispatchers.IO) {
            userPreferences.saveCalendarStartDayOfWeekPreference(preference)
        }
    }

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun saveReminderNotificationPreference(preference: ReminderNotificationPreference) {
        withContext(Dispatchers.IO) {
            userPreferences.saveReminderNotificationPreference(preference)
        }
    }

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun savePasscodeLockPreference(preference: PassCodeLockPreference) {
        withContext(Dispatchers.IO) {
            userPreferences.savePasscodeLockPreference(preference)
        }
    }

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun saveWeatherInfoAcquisitionPreference(preference: WeatherInfoAcquisitionPreference) {
        withContext(Dispatchers.IO) {
            userPreferences.saveWeatherInfoAcquisitionPreference(preference)
        }
    }

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun initializeAllPreferences() {
        withContext(Dispatchers.IO) {
            userPreferences.initializeAllPreferences()
        }
    }
}
