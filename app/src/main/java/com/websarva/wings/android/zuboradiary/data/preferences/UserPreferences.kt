package com.websarva.wings.android.zuboradiary.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
//      その為、@Suppress("RedundantSuppression")で警告回避。
@Suppress( "unused", "RedundantSuppression") //MEMO:警告対策。(初期化してない為、Unusedの警告が表示される)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferences @Inject constructor(private val context: Context) {

    private val logTag = createLogTag()

    @Throws(Throwable::class)
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
                AllPreferences(preferences)
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

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun saveCalendarStartDayOfWeekPreference(value: CalendarStartDayOfWeekPreference) {
        context.dataStore.edit { preferences ->
            value.setUpPreferences(preferences)
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

    @Throws(
        IOException::class,
        Exception::class
    )
    suspend fun savePasscodeLockPreference(value: PassCodeLockPreference) {
        context.dataStore.edit { preferences ->
            value.setUpPreferences(preferences)
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
