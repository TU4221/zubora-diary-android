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
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
//      その為、@Suppress("RedundantSuppression")で警告回避。
@Suppress( "unused", "RedundantSuppression") //MEMO:警告対策。(初期化してない為、Unusedの警告が表示される)
internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

internal class UserPreferences @Inject constructor(private val context: Context) {

    private val logTag = createLogTag()

    private val themeColorPreferenceKey = intPreferencesKey("theme_color")

    private val calendarStartDayOfWeekPreferenceKey =
        intPreferencesKey("calendar_start_day_of_week")

    private val isCheckedReminderNotificationPreferenceKey =
        booleanPreferencesKey("is_checked_reminder_notification")
    private val reminderNotificationTimePreferenceKey =
        stringPreferencesKey("reminder_notification_time")

    private val isCheckedPasscodeLockPreferenceKey =
        booleanPreferencesKey("is_checked_passcode_lock")
    private val passcodePreferenceKey = stringPreferencesKey("passcode")

    private val isCheckedWeatherInfoAcquisitionPreferenceKey =
        booleanPreferencesKey("is_checked_weather_info_acquisition")

    fun loadAllPreferences(): Flow<AllPreferences> {
        return context.dataStore.data
            .catch { cause ->
                if (cause is IOException) {
                    Log.e(logTag, "アプリ設定値読込_失敗", cause)
                    emit(emptyPreferences())
                } else {
                    throw cause
                }
            }
            .map { preferences ->
                AllPreferences(
                    createThemeColorPreference(preferences),
                    createCalendarStartDayOfWeekPreference(preferences),
                    createReminderNotificationPreference(preferences),
                    createPasscodeLockPreference(preferences),
                    createWeatherInfoAcquisitionPreference(preferences)
                )
            }
    }

    private fun createThemeColorPreference(preferences: Preferences): ThemeColorPreference {
        val themeColorNumber =
            preferences[themeColorPreferenceKey]
                ?: ThemeColorPreference.THEME_COLOR_DEFAULT_VALUE
        return ThemeColorPreference(themeColorNumber)
    }

    private fun createCalendarStartDayOfWeekPreference(
        preferences: Preferences
    ): CalendarStartDayOfWeekPreference {
        val dayOfWeekNumber =
            preferences[calendarStartDayOfWeekPreferenceKey]
                ?: CalendarStartDayOfWeekPreference.DAY_OF_WEEK_DEFAULT_VALUE
        return CalendarStartDayOfWeekPreference(dayOfWeekNumber)
    }

    private fun createReminderNotificationPreference(
        preferences: Preferences
    ): ReminderNotificationPreference {
        var isCheckedReminder = preferences[isCheckedReminderNotificationPreferenceKey]
        var notificationTimeString = preferences[reminderNotificationTimePreferenceKey]
        if (isCheckedReminder == null || notificationTimeString == null) {
            isCheckedReminder = ReminderNotificationPreference.IS_CHECKED_DEFAULT_VALUE
            notificationTimeString = ReminderNotificationPreference.NOTIFICATION_TIME_DEFAULT_VALUE
        }
        return ReminderNotificationPreference(isCheckedReminder, notificationTimeString)
    }

    private fun createPasscodeLockPreference(preferences: Preferences): PassCodeLockPreference {
        var isCheckedPasscode = preferences[isCheckedPasscodeLockPreferenceKey]
        var passCode = preferences[passcodePreferenceKey]
        if (isCheckedPasscode == null || passCode == null) {
            isCheckedPasscode = PassCodeLockPreference.IS_CHECKED_DEFAULT_VALUE
            passCode = PassCodeLockPreference.PASS_CODE_DEFAULT_VALUE
        }
        return PassCodeLockPreference(isCheckedPasscode, passCode)
    }

    private fun createWeatherInfoAcquisitionPreference(
        preferences: Preferences
    ): WeatherInfoAcquisitionPreference {
        val isCheckedWeather =
            preferences[isCheckedWeatherInfoAcquisitionPreferenceKey]
                ?: WeatherInfoAcquisitionPreference.IS_CHECKED_DEFAULT_VALUE
        return WeatherInfoAcquisitionPreference(isCheckedWeather)
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
        preferences[isCheckedReminderNotificationPreferenceKey] = value.isChecked
        preferences[reminderNotificationTimePreferenceKey] = value.notificationTimeString
    }

    @Throws(UserPreferencesAccessException::class)
    suspend fun savePasscodeLockPreference(value: PassCodeLockPreference) {
        executeDataStoreEditOperation { preferences ->
            savePasscodeLockPreferenceValue(preferences, value)
        }
    }

    private fun savePasscodeLockPreferenceValue(
        preferences: MutablePreferences,
        value: PassCodeLockPreference
    ) {
        preferences[isCheckedPasscodeLockPreferenceKey] = value.isChecked
        preferences[passcodePreferenceKey] = value.passCode
    }

    @Throws(UserPreferencesAccessException::class)
    suspend fun saveWeatherInfoAcquisitionPreference(value: WeatherInfoAcquisitionPreference) {
        executeDataStoreEditOperation { preferences ->
            saveWeatherInfoAcquisitionPreferenceValue(preferences, value)
        }
    }

    private fun saveWeatherInfoAcquisitionPreferenceValue(
        preferences: MutablePreferences,
        value: WeatherInfoAcquisitionPreference
    ) {
        preferences[isCheckedWeatherInfoAcquisitionPreferenceKey] = value.isChecked
    }

    suspend fun initializeAllPreferences() {
        executeDataStoreEditOperation { preferences ->
            saveThemeColorPreferenceValue(preferences, ThemeColorPreference())
            saveCalendarStartDayOfWeekPreferenceValue(preferences, CalendarStartDayOfWeekPreference())
            saveReminderNotificationPreferenceValue(preferences, ReminderNotificationPreference())
            savePasscodeLockPreferenceValue(preferences, PassCodeLockPreference())
            saveWeatherInfoAcquisitionPreferenceValue(preferences, WeatherInfoAcquisitionPreference())
        }
    }
}
