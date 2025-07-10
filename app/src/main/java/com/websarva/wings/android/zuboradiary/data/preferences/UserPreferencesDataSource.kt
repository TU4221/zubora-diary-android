package com.websarva.wings.android.zuboradiary.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.websarva.wings.android.zuboradiary.di.ApplicationScope
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
//      その為、@Suppress("RedundantSuppression")で警告回避。
@Suppress( "unused", "RedundantSuppression") //MEMO:警告対策。(初期化してない為、Unusedの警告が表示される)
internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

internal class UserPreferences @Inject constructor(
    private val context: Context,
    @ApplicationScope private val appScope: CoroutineScope
) {

    private val logTag = createLogTag()

    private val themeColorPreferenceKey = intPreferencesKey("theme_color")

    private val calendarStartDayOfWeekPreferenceKey =
        intPreferencesKey("calendar_start_day_of_week")

    private val isEnabledReminderNotificationPreferenceKey =
        booleanPreferencesKey("is_enabled_reminder_notification")
    private val reminderNotificationTimePreferenceKey =
        stringPreferencesKey("reminder_notification_time")

    private val isEnabledPasscodeLockPreferenceKey =
        booleanPreferencesKey("is_enabled_passcode_lock")
    private val passcodePreferenceKey = stringPreferencesKey("passcode")

    private val isEnabledWeatherInfoFetchPreferenceKey =
        booleanPreferencesKey("is_enabled_weather_info_fetch")

    private val userPreferencesFlow =
        context.dataStore.data
            .map { preferences ->
                Log.e(logTag, "アプリ設定値読込_成功_$preferences")
                val result =
                    UserPreferencesFetchResult
                        .Success(
                            preferences
                        ) as UserPreferencesFetchResult
                return@map result
            }.catch { cause ->
                Log.e(logTag, "アプリ設定値読込_失敗", cause)
                if (cause !is IOException) throw cause

                emit(
                    UserPreferencesFetchResult
                        .Failure(
                            UserPreferencesAccessException(cause),
                            emptyPreferences()
                        )
                )
            }.stateIn(
                appScope,
                SharingStarted.Eagerly,
                null
            ).filterNotNull()

    fun fetchThemeColorPreference(): Flow<UserPreferenceFlowResult<ThemeColorPreference>> {
        return userPreferencesFlow.map { result ->
            when (result) {
                is UserPreferencesFetchResult.Success -> {
                    UserPreferenceFlowResult.Success(
                        createThemeColorPreference(result.preferences)
                    )
                }
                is UserPreferencesFetchResult.Failure -> {
                    UserPreferenceFlowResult.Failure(
                        result.exception,
                        createThemeColorPreference(result.fallbackPreferences)
                    )
                }
            }
        }
    }

    private fun createThemeColorPreference(preferences: Preferences): ThemeColorPreference {
        val themeColorNumber =
            preferences[themeColorPreferenceKey]
                ?: return ThemeColorPreference()
        return ThemeColorPreference(themeColorNumber)
    }

    fun fetchCalendarStartDayOfWeekPreference():
            Flow<UserPreferenceFlowResult<CalendarStartDayOfWeekPreference>> {
        return userPreferencesFlow.map { result ->
            when (result) {
                is UserPreferencesFetchResult.Success -> {
                    UserPreferenceFlowResult.Success(
                        createCalendarStartDayOfWeekPreference(result.preferences)
                    )
                }
                is UserPreferencesFetchResult.Failure -> {
                    UserPreferenceFlowResult.Failure(
                        result.exception,
                        createCalendarStartDayOfWeekPreference(result.fallbackPreferences)
                    )
                }
            }
        }
    }

    private fun createCalendarStartDayOfWeekPreference(
        preferences: Preferences
    ): CalendarStartDayOfWeekPreference {
        val dayOfWeekNumber =
            preferences[calendarStartDayOfWeekPreferenceKey]
                ?: return CalendarStartDayOfWeekPreference()
        return CalendarStartDayOfWeekPreference(dayOfWeekNumber)
    }

    fun fetchReminderNotificationPreference():
            Flow<UserPreferenceFlowResult<ReminderNotificationPreference>> {
        return userPreferencesFlow.map { result ->
            when (result) {
                is UserPreferencesFetchResult.Success -> {
                    UserPreferenceFlowResult.Success(
                        createReminderNotificationPreference(result.preferences)
                    )
                }
                is UserPreferencesFetchResult.Failure -> {
                    UserPreferenceFlowResult.Failure(
                        result.exception,
                        createReminderNotificationPreference(result.fallbackPreferences)
                    )
                }
            }
        }
    }

    private fun createReminderNotificationPreference(
        preferences: Preferences
    ): ReminderNotificationPreference {
        val isEnabled =
            preferences[isEnabledReminderNotificationPreferenceKey]
                ?: return ReminderNotificationPreference()
        val notificationTimeString =
            preferences[reminderNotificationTimePreferenceKey]
                ?: return ReminderNotificationPreference()
        return ReminderNotificationPreference(isEnabled, notificationTimeString)
    }

    fun fetchPasscodeLockPreference():
            Flow<UserPreferenceFlowResult<PasscodeLockPreference>> {
        return userPreferencesFlow.map { result ->
            when (result) {
                is UserPreferencesFetchResult.Success -> {
                    UserPreferenceFlowResult.Success(
                        createPasscodeLockPreference(result.preferences)
                    )
                }
                is UserPreferencesFetchResult.Failure -> {
                    UserPreferenceFlowResult.Failure(
                        result.exception,
                        createPasscodeLockPreference(result.fallbackPreferences)
                    )
                }
            }
        }
    }

    private fun createPasscodeLockPreference(preferences: Preferences): PasscodeLockPreference {
        val isEnabled =
            preferences[isEnabledPasscodeLockPreferenceKey] ?: return PasscodeLockPreference()
        val passCode = preferences[passcodePreferenceKey] ?: return PasscodeLockPreference()
        return PasscodeLockPreference(isEnabled, passCode)
    }

    fun fetchWeatherInfoFetchPreference():
            Flow<UserPreferenceFlowResult<WeatherInfoFetchPreference>> {
        return userPreferencesFlow.map { result ->
            when (result) {
                is UserPreferencesFetchResult.Success -> {
                    UserPreferenceFlowResult.Success(
                        createWeatherInfoFetchPreference(result.preferences)
                    )
                }
                is UserPreferencesFetchResult.Failure -> {
                    UserPreferenceFlowResult.Failure(
                        result.exception,
                        createWeatherInfoFetchPreference(result.fallbackPreferences)
                    )
                }
            }
        }
    }

    private fun createWeatherInfoFetchPreference(
        preferences: Preferences
    ): WeatherInfoFetchPreference {
        val isEnabled =
            preferences[isEnabledWeatherInfoFetchPreferenceKey]
                ?: return WeatherInfoFetchPreference()
        return WeatherInfoFetchPreference(isEnabled)
    }

    @Throws(UserPreferencesAccessException::class)
    private suspend fun executeDataStoreEditOperation(
        operation: suspend (MutablePreferences) -> Unit
    ): Preferences {
        return try {
            context.dataStore.edit { preferences ->
                operation(preferences)
            }
        } catch (e: IOException) {
            throw UserPreferencesAccessException(e)
        }
    }

    @Throws(UserPreferencesAccessException::class)
    suspend fun saveThemeColorPreference(value: ThemeColorPreference) {
        executeDataStoreEditOperation { preferences ->
            saveThemeColorPreferenceValue(preferences, value)
        }
    }

    private fun saveThemeColorPreferenceValue(
        preferences: MutablePreferences,
        value: ThemeColorPreference
    ) {
        preferences[themeColorPreferenceKey] = value.themeColorNumber
    }

    @Throws(UserPreferencesAccessException::class)
    suspend fun saveCalendarStartDayOfWeekPreference(value: CalendarStartDayOfWeekPreference) {
        executeDataStoreEditOperation { preferences ->
            saveCalendarStartDayOfWeekPreferenceValue(preferences, value)
        }
    }

    private fun saveCalendarStartDayOfWeekPreferenceValue(
        preferences: MutablePreferences,
        value: CalendarStartDayOfWeekPreference
    ) {
        preferences[calendarStartDayOfWeekPreferenceKey] = value.dayOfWeekNumber
    }

    @Throws(UserPreferencesAccessException::class)
    suspend fun saveReminderNotificationPreference(value: ReminderNotificationPreference) {
        executeDataStoreEditOperation { preferences ->
            saveReminderNotificationPreferenceValue(preferences, value)
        }
    }

    private fun saveReminderNotificationPreferenceValue(
        preferences: MutablePreferences,
        value: ReminderNotificationPreference
    ) {
        preferences[isEnabledReminderNotificationPreferenceKey] = value.isEnabled
        preferences[reminderNotificationTimePreferenceKey] = value.notificationTimeString
    }

    @Throws(UserPreferencesAccessException::class)
    suspend fun savePasscodeLockPreference(value: PasscodeLockPreference) {
        executeDataStoreEditOperation { preferences ->
            savePasscodeLockPreferenceValue(preferences, value)
        }
    }

    private fun savePasscodeLockPreferenceValue(
        preferences: MutablePreferences,
        value: PasscodeLockPreference
    ) {
        preferences[isEnabledPasscodeLockPreferenceKey] = value.isEnabled
        preferences[passcodePreferenceKey] = value.passcode
    }

    @Throws(UserPreferencesAccessException::class)
    suspend fun saveWeatherInfoFetchPreference(value: WeatherInfoFetchPreference) {
        executeDataStoreEditOperation { preferences ->
            saveWeatherInfoFetchPreferenceValue(preferences, value)
        }
    }

    private fun saveWeatherInfoFetchPreferenceValue(
        preferences: MutablePreferences,
        value: WeatherInfoFetchPreference
    ) {
        preferences[isEnabledWeatherInfoFetchPreferenceKey] = value.isEnabled
    }

    suspend fun initializeAllPreferences() {
        executeDataStoreEditOperation { preferences ->
            saveThemeColorPreferenceValue(preferences, ThemeColorPreference())
            saveCalendarStartDayOfWeekPreferenceValue(preferences, CalendarStartDayOfWeekPreference())
            saveReminderNotificationPreferenceValue(preferences, ReminderNotificationPreference())
            savePasscodeLockPreferenceValue(preferences, PasscodeLockPreference())
            saveWeatherInfoFetchPreferenceValue(preferences, WeatherInfoFetchPreference())
        }
    }
}
