package com.websarva.wings.android.zuboradiary.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@Suppress( "unused") //MEMO:警告対策。(初期化してない為、Unusedの警告が表示される)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferences @Inject constructor(private val context: Context) {

    @Throws(Throwable::class)
    private fun Flow<Preferences>.setUpIOExceptionHandling(): Flow<Preferences> {
        return this.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
    }

    // MEMO:初回読込は"null"が返ってくるので、その場合は初期値を返す。(他のPreferenceValueも同様)
    @Throws(Throwable::class)
    fun loadThemeColorPreference(): Flow<ThemeColorPreference> {
        return context.dataStore.data.setUpIOExceptionHandling()
            .map { preferences ->
                ThemeColorPreference(preferences)
            }
    }

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun saveThemeColorPreference(value: ThemeColorPreference) {
        context.dataStore.edit { preferences ->
            value.setUpPreferences(preferences)
        }
    }

    @Throws(Throwable::class)
    fun loadCalendarStartDayOfWeekPreference(): Flow<CalendarStartDayOfWeekPreference> {
        return context.dataStore.data.setUpIOExceptionHandling()
            .map { preferences ->
                CalendarStartDayOfWeekPreference(preferences)
            }
    }

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun saveCalendarStartDayOfWeekPreference(value: CalendarStartDayOfWeekPreference) {
        context.dataStore.edit { preferences ->
            value.setUpPreferences(preferences)
        }
    }

    @Throws(Throwable::class)
    fun loadReminderNotificationPreference(): Flow<ReminderNotificationPreference> {
        return context.dataStore.data.setUpIOExceptionHandling()
            .map { preferences ->
                ReminderNotificationPreference(preferences)
            }
    }

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun saveReminderNotificationPreference(value: ReminderNotificationPreference) {
        context.dataStore.edit { preferences ->
            value.setUpPreferences(preferences)
        }
    }

    @Throws(Throwable::class)
    fun loadPasscodeLockPreference(): Flow<PassCodeLockPreference> {
        return context.dataStore.data.setUpIOExceptionHandling()
            .map { preferences ->
                PassCodeLockPreference(preferences)
            }
    }

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun savePasscodeLockPreference(value: PassCodeLockPreference) {
        context.dataStore.edit { preferences ->
            value.setUpPreferences(preferences)
        }
    }

    @Throws(Throwable::class)
    fun loadWeatherInfoAcquisitionPreference(): Flow<WeatherInfoAcquisitionPreference> {
        return context.dataStore.data.setUpIOExceptionHandling()
            .map { preferences ->
                WeatherInfoAcquisitionPreference(preferences)
            }
    }

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun saveWeatherInfoAcquisitionPreference(value: WeatherInfoAcquisitionPreference) {
        context.dataStore.edit { preferences ->
            value.setUpPreferences(preferences)
        }
    }


    suspend fun initializeAllPreferences() {
        context.dataStore.edit { preferences ->
            ThemeColorPreference().setUpPreferences(preferences)
            CalendarStartDayOfWeekPreference().setUpPreferences(preferences)
            ReminderNotificationPreference().setUpPreferences(preferences)
            PassCodeLockPreference().setUpPreferences(preferences)
            WeatherInfoAcquisitionPreference().setUpPreferences(preferences)
        }
    }
}
