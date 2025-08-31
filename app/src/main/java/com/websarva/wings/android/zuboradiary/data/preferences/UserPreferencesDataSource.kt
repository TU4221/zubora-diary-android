package com.websarva.wings.android.zuboradiary.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

/**
 * [Context] の拡張プロパティとして、
 * アプリケーションのユーザー設定[Preferences]を格納する [DataStore] インスタンスを提供する。
 *
 * このDataStoreは "UserPreferences" という名前で識別される。
 */
// MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
//      その為、@Suppress("RedundantSuppression")で警告回避。
@Suppress( "unused", "RedundantSuppression") //MEMO:警告対策。(初期化してない為、Unusedの警告が表示される)
internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "UserPreferences")

/**
 * ユーザー設定 (Preferences DataStore) へのアクセスを提供するデータソースクラス。
 *
 * このクラスは、テーマカラー、カレンダー設定、通知設定など、アプリケーション全体のユーザー設定の
 * 読み書きを担当する。各設定項目は、対応する [UserPreference] を実装したデータクラスとして扱う。
 *
 * 設定の読み込みはFlowとして提供し、変更をリアクティブに監視できる。
 * 設定の書き込みはsuspend関数として提供。
 *
 * @property context アプリケーションコンテキスト。DataStoreインスタンスの取得に使用。
 * @property appScope アプリケーションスコープのコルーチンスコープ。Flowの共有などに使用。
 */
internal class UserPreferencesDataSource @Inject constructor(
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

    /**
     * DataStoreから全てのユーザー設定を読み込み、その結果を[UserPreferencesLoadResult]として
     * [kotlinx.coroutines.flow.StateFlow]で提供する内部Flow。
     *
     * このFlowはアプリケーションスコープでEagerlyに開始され、最新の設定状態を保持する。
     * DataStoreアクセス時に[IOException]が発生した場合は、[UserPreferencesLoadResult.Failure]をemitする。
     * それ以外の例外は再スローされる。
     */
    private val userPreferencesFlow =
        context.dataStore.data
            .map { preferences ->
                Log.e(logTag, "アプリ設定値読込_成功_$preferences")
                val result =
                    UserPreferencesLoadResult
                        .Success(
                            preferences
                        ) as UserPreferencesLoadResult
                return@map result
            }.catch { cause ->
                Log.e(logTag, "アプリ設定値読込_失敗", cause)
                if (cause !is IOException) throw cause

                emit(
                    UserPreferencesLoadResult
                        .Failure(UserPreferencesException.DataStoreAccessFailure(cause))
                )
            }.stateIn(
                appScope,
                SharingStarted.Eagerly,
                null
            ).filterNotNull() // 初期値nullを除外

    /**
     * テーマカラー設定 ([ThemeColorPreference]) をFlowとして読み込む。
     *
     * データが存在しない場合は [UserPreferenceFlowResult.Failure] に
     * [UserPreferencesException.DataNotFound] を設定して返す。
     *
     * @return テーマカラー設定の読み込み結果を通知するFlow。
     */
    fun loadThemeColorPreference(): Flow<UserPreferenceFlowResult<ThemeColorPreference>> {
        return userPreferencesFlow.map { result ->
            when (result) {
                is UserPreferencesLoadResult.Success -> {
                    val preference = createThemeColorPreference(result.preferences)
                    if (preference != null) {
                        UserPreferenceFlowResult.Success(preference)
                    } else {
                        UserPreferenceFlowResult.Failure(
                            UserPreferencesException.DataNotFound("テーマカラー")
                        )
                    }
                }
                is UserPreferencesLoadResult.Failure -> {
                    UserPreferenceFlowResult.Failure(result.exception)
                }
            }
        }
    }

    /**
     * [Preferences] オブジェクトから [ThemeColorPreference] を生成する。
     *
     * @param preferences DataStoreから読み込まれたPreferencesオブジェクト。
     * @return 生成された [ThemeColorPreference]、またはデータが存在しない場合はnull。
     */
    private fun createThemeColorPreference(preferences: Preferences): ThemeColorPreference? {
        val themeColorNumber = preferences[themeColorPreferenceKey] ?: return null
        return ThemeColorPreference(themeColorNumber)
    }

    /**
     * カレンダーの開始曜日設定 ([CalendarStartDayOfWeekPreference]) をFlowとして読み込む。
     *
     * データが存在しない場合は [UserPreferenceFlowResult.Failure] に
     * [UserPreferencesException.DataNotFound] を設定して返す。
     *
     * @return カレンダー開始曜日設定の読み込み結果を通知するFlow。
     */
    fun loadCalendarStartDayOfWeekPreference():
            Flow<UserPreferenceFlowResult<CalendarStartDayOfWeekPreference>> {
        return userPreferencesFlow.map { result ->
            when (result) {
                is UserPreferencesLoadResult.Success -> {
                    val preference = createCalendarStartDayOfWeekPreference(result.preferences)
                    if (preference != null) {
                        UserPreferenceFlowResult.Success(preference)
                    } else {
                        UserPreferenceFlowResult.Failure(
                            UserPreferencesException.DataNotFound("カレンダー開始曜日")
                        )
                    }
                }
                is UserPreferencesLoadResult.Failure -> {
                    UserPreferenceFlowResult.Failure(
                        result.exception
                    )
                }
            }
        }
    }

    /**
     * [Preferences] オブジェクトから [CalendarStartDayOfWeekPreference] を生成する。
     *
     * @param preferences DataStoreから読み込まれたPreferencesオブジェクト。
     * @return 生成された [CalendarStartDayOfWeekPreference]、またはデータが存在しない場合はnull。
     */
    private fun createCalendarStartDayOfWeekPreference(
        preferences: Preferences
    ): CalendarStartDayOfWeekPreference? {
        val dayOfWeekNumber =
            preferences[calendarStartDayOfWeekPreferenceKey] ?: return null
        return CalendarStartDayOfWeekPreference(dayOfWeekNumber)
    }

    /**
     * リマインダー通知設定 ([ReminderNotificationPreference]) をFlowとして読み込む。

     * データが存在しない場合は [UserPreferenceFlowResult.Failure] に
     * [UserPreferencesException.DataNotFound] を設定して返す。
     *
     * @return リマインダー通知設定の読み込み結果を通知するFlow。
     */
    fun loadReminderNotificationPreference():
            Flow<UserPreferenceFlowResult<ReminderNotificationPreference>> {
        return userPreferencesFlow.map { result ->
            when (result) {
                is UserPreferencesLoadResult.Success -> {
                    val preference = createReminderNotificationPreference(result.preferences)
                    if (preference != null) {
                        UserPreferenceFlowResult.Success(preference)
                    } else {
                        UserPreferenceFlowResult.Failure(
                            UserPreferencesException.DataNotFound("リマインダー通知")
                        )
                    }
                }
                is UserPreferencesLoadResult.Failure -> {
                    UserPreferenceFlowResult.Failure(
                        result.exception
                    )
                }
            }
        }
    }

    /**
     * [Preferences] オブジェクトから [ReminderNotificationPreference] を生成する。
     *
     * @param preferences DataStoreから読み込まれたPreferencesオブジェクト。
     * @return 生成された [ReminderNotificationPreference]、またはデータが存在しない場合はnull。
     */
    private fun createReminderNotificationPreference(
        preferences: Preferences
    ): ReminderNotificationPreference? {
        val isEnabled =
            preferences[isEnabledReminderNotificationPreferenceKey] ?: return null
        val notificationTimeString =
            preferences[reminderNotificationTimePreferenceKey] ?: return null
        return ReminderNotificationPreference(isEnabled, notificationTimeString)
    }

    /**
     * パスコードロック設定 ([PasscodeLockPreference]) をFlowとして読み込む。
     *
     * データが存在しない場合は [UserPreferenceFlowResult.Failure] に
     * [UserPreferencesException.DataNotFound] を設定して返す。
     *
     * @return パスコードロック設定の読み込み結果を通知するFlow。
     */
    fun loadPasscodeLockPreference():
            Flow<UserPreferenceFlowResult<PasscodeLockPreference>> {
        return userPreferencesFlow.map { result ->
            when (result) {
                is UserPreferencesLoadResult.Success -> {
                    val preference = createPasscodeLockPreference(result.preferences)
                    if (preference != null) {
                        UserPreferenceFlowResult.Success(preference)
                    } else {
                        UserPreferenceFlowResult.Failure(
                            UserPreferencesException.DataNotFound("パスコードロック")
                        )
                    }
                }
                is UserPreferencesLoadResult.Failure -> {
                    UserPreferenceFlowResult.Failure(
                        result.exception
                    )
                }
            }
        }
    }

    /**
     * [Preferences] オブジェクトから [PasscodeLockPreference] を生成する。
     *
     * @param preferences DataStoreから読み込まれたPreferencesオブジェクト。
     * @return 生成された [PasscodeLockPreference]、またはデータが存在しない場合はnull。
     */
    private fun createPasscodeLockPreference(preferences: Preferences): PasscodeLockPreference? {
        val isEnabled = preferences[isEnabledPasscodeLockPreferenceKey] ?: return null
        val passCode = preferences[passcodePreferenceKey] ?: return null
        return PasscodeLockPreference(isEnabled, passCode)
    }

    /**
     * 天気情報取得設定 ([WeatherInfoFetchPreference]) をFlowとして読み込む。
     *
     * データが存在しない場合は [UserPreferenceFlowResult.Failure] に
     * [UserPreferencesException.DataNotFound] を設定して返す。
     *
     * @return 天気情報取得設定の読み込み結果を通知するFlow。
     */
    fun loadWeatherInfoFetchPreference():
            Flow<UserPreferenceFlowResult<WeatherInfoFetchPreference>> {
        return userPreferencesFlow.map { result ->
            when (result) {
                is UserPreferencesLoadResult.Success -> {
                    val preference = createWeatherInfoFetchPreference(result.preferences)
                    if (preference != null) {
                        UserPreferenceFlowResult.Success(preference)
                    } else {
                        UserPreferenceFlowResult.Failure(
                            UserPreferencesException.DataNotFound("天気情報取得")
                        )
                    }
                }
                is UserPreferencesLoadResult.Failure -> {
                    UserPreferenceFlowResult.Failure(result.exception)
                }
            }
        }
    }

    /**
     * [Preferences] オブジェクトから [WeatherInfoFetchPreference] を生成する。
     *
     * @param preferences DataStoreから読み込まれたPreferencesオブジェクト。
     * @return 生成された [WeatherInfoFetchPreference]、またはデータが存在しない場合はnull。
     */
    private fun createWeatherInfoFetchPreference(
        preferences: Preferences
    ): WeatherInfoFetchPreference? {
        val isEnabled = preferences[isEnabledWeatherInfoFetchPreferenceKey] ?: return null
        return WeatherInfoFetchPreference(isEnabled)
    }

    /**
     * テーマカラー設定 ([ThemeColorPreference]) をDataStoreに保存する。
     *
     * @param value 保存するテーマカラー設定。
     * @throws UserPreferencesException.DataStoreAccessFailure DataStoreへの書き込みに失敗した場合。
     */
    @Throws(UserPreferencesException.DataStoreAccessFailure::class)
    suspend fun saveThemeColorPreference(value: ThemeColorPreference) {
        executeDataStoreEditOperation { preferences ->
            preferences[themeColorPreferenceKey] = value.themeColorNumber
        }
    }

    /**
     * カレンダーの開始曜日設定 ([CalendarStartDayOfWeekPreference]) をDataStoreに保存する。
     *
     * @param value 保存するカレンダー開始曜日設定。
     * @throws UserPreferencesException.DataStoreAccessFailure DataStoreへの書き込みに失敗した場合。
     */
    @Throws(UserPreferencesException.DataStoreAccessFailure::class)
    suspend fun saveCalendarStartDayOfWeekPreference(value: CalendarStartDayOfWeekPreference) {
        executeDataStoreEditOperation { preferences ->
            preferences[calendarStartDayOfWeekPreferenceKey] = value.dayOfWeekNumber
        }
    }

    /**
     * リマインダー通知設定 ([ReminderNotificationPreference]) をDataStoreに保存する。
     *
     * @param value 保存するリマインダー通知設定。
     * @throws UserPreferencesException.DataStoreAccessFailure DataStoreへの書き込みに失敗した場合。
     */
    @Throws(UserPreferencesException.DataStoreAccessFailure::class)
    suspend fun saveReminderNotificationPreference(value: ReminderNotificationPreference) {
        executeDataStoreEditOperation { preferences ->
            preferences[isEnabledReminderNotificationPreferenceKey] = value.isEnabled
            preferences[reminderNotificationTimePreferenceKey] = value.notificationTimeString
        }
    }

    /**
     * パスコードロック設定 ([PasscodeLockPreference]) をDataStoreに保存する。
     *
     * @param value 保存するパスコードロック設定。
     * @throws UserPreferencesException.DataStoreAccessFailure DataStoreへの書き込みに失敗した場合。
     */
    @Throws(UserPreferencesException.DataStoreAccessFailure::class)
    suspend fun savePasscodeLockPreference(value: PasscodeLockPreference) {
        executeDataStoreEditOperation { preferences ->
            preferences[isEnabledPasscodeLockPreferenceKey] = value.isEnabled
            preferences[passcodePreferenceKey] = value.passcode
        }
    }

    /**
     * 天気情報取得設定 ([WeatherInfoFetchPreference]) をDataStoreに保存する。
     *
     * @param value 保存する天気情報取得設定。
     * @throws UserPreferencesException.DataStoreAccessFailure DataStoreへの書き込みに失敗した場合。
     */
    @Throws(UserPreferencesException.DataStoreAccessFailure::class)
    suspend fun saveWeatherInfoFetchPreference(value: WeatherInfoFetchPreference) {
        executeDataStoreEditOperation { preferences ->
            preferences[isEnabledWeatherInfoFetchPreferenceKey] = value.isEnabled
        }
    }

    /**
     * DataStoreの編集操作を実行するための共通ヘルパー関数。
     *
     * 指定された [operation] を [DataStore.edit] ブロック内で実行する。
     * [IOException] が発生した場合は、それをキャッチし、
     * [UserPreferencesException.DataStoreAccessFailure] としてラップして再スローする。
     *
     * @param operation [MutablePreferences] を引数に取り、DataStoreへの書き込み処理を行うsuspend関数。
     * @return DataStoreの編集操作後の [Preferences] オブジェクト。
     * @throws UserPreferencesException.DataStoreAccessFailure DataStoreへの書き込みに失敗した場合。
     */
    private suspend fun executeDataStoreEditOperation(
        operation: suspend (MutablePreferences) -> Unit
    ): Preferences {
        return try {
            context.dataStore.edit { preferences ->
                operation(preferences)
            }
        } catch (e: IOException) {
            throw UserPreferencesException.DataStoreAccessFailure(e)
        }
    }
}
