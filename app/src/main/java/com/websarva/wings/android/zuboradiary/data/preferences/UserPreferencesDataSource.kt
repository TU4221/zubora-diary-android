package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.websarva.wings.android.zuboradiary.data.preferences.exception.DataNotFoundException
import com.websarva.wings.android.zuboradiary.data.preferences.exception.DataStoreReadException
import com.websarva.wings.android.zuboradiary.data.preferences.exception.DataStoreWriteException
import com.websarva.wings.android.zuboradiary.di.ApplicationScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject
/**
 * ユーザー設定 (Preferences DataStore) へのアクセスを提供するデータソースクラス。
 *
 * このクラスは、テーマカラー、カレンダー設定、通知設定など、アプリケーション全体のユーザー設定の
 * 読み書きを担当する。各設定項目は、対応する [UserPreference] を実装したデータクラスとして扱う。
 *
 * 設定の読み込みはFlowとして提供し、変更をリアクティブに監視できる。
 * 設定の書き込みはsuspend関数として提供。
 *
 * @property preferencesDataStore ユーザー設定を永続化するためのに使用される。
 * @property appScope アプリケーションスコープのコルーチンスコープ。Flowの共有などに使用される。
 * @property dispatcher ユーザー設定の操作を実行するスレッドプール。
 */
internal class UserPreferencesDataSource @Inject constructor(
    private val preferencesDataStore: DataStore<Preferences>,
    @ApplicationScope private val appScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

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
     * [StateFlow]で提供するFlow。
     *
     * このFlowは [stateIn] オペレータによって [StateFlow] に変換され、アプリケーションスコープで
     * Eagerlyに共有が開始される。これにより、複数の箇所から参照された場合でも、
     * DataStoreへの実際の読み込み処理は一度となり、最新の設定状態が効率的に共有・保持される。
     *
     * 正常に読み込めた場合は [UserPreferencesLoadResult.Success] に [Preferences] を格納して放出する。
     * DataStoreアクセス時に [IOException] が発生した場合は、[UserPreferencesLoadResult.Failure] に
     * [DataStoreReadException] を格納して放出する。
     * それ以外の予期せぬ例外はそのまま再スローされ、Flowを失敗させる。
     *
     * @return 最新のユーザー設定の読み込み結果 ([UserPreferencesLoadResult]) を保持し放出する [StateFlow]。
     */
    private val userPreferencesResultFlow =
        preferencesDataStore.data
            .map { preferences ->
                val result =
                    UserPreferencesLoadResult
                        .Success(
                            preferences
                        ) as UserPreferencesLoadResult
                return@map result
            }.catch { cause ->
                if (cause !is IOException) throw cause

                emit(
                    UserPreferencesLoadResult
                        .Failure(DataStoreReadException(cause = cause))
                )
            }.stateIn(
                appScope,
                SharingStarted.Eagerly,
                null
            ).filterNotNull() // 初期値nullを除外

    /**
     * テーマカラー設定 ([ThemeColorPreference]) をFlowとして読み込む。
     *
     * [userPreferencesResultFlow] から取得した [UserPreferencesLoadResult] をもとに設定オブジェクトを生成する。
     *
     * @return テーマカラー設定 ([ThemeColorPreference]) を放出するFlow。
     * @throws DataStoreReadException データストアへの読み込み(アクセス)に失敗した場合。([Flow] 内部で発生)
     * @throws DataNotFoundException 対応するデータが存在しない場合。([Flow] 内部で発生)
     */
    fun loadThemeColorPreference(): Flow<ThemeColorPreference> {
        return userPreferencesResultFlow.map { result ->
            when (result) {
                is UserPreferencesLoadResult.Success -> createThemeColorPreference(result.preferences)
                is UserPreferencesLoadResult.Failure -> throw result.exception
            }
        }
    }

    /**
     * [Preferences] オブジェクトから [ThemeColorPreference] を生成する。
     *
     * @param preferences DataStoreから読み込まれたPreferencesオブジェクト。
     * @return 生成された [ThemeColorPreference]。
     * @throws DataNotFoundException 対応するキーのデータが存在しない場合。
     */
    private fun createThemeColorPreference(preferences: Preferences): ThemeColorPreference {
        val themeColorNumber = preferences[themeColorPreferenceKey]
            ?: throw DataNotFoundException("テーマカラー")
        return ThemeColorPreference(themeColorNumber)
    }

    /**
     * カレンダーの開始曜日設定 ([CalendarStartDayOfWeekPreference]) をFlowとして読み込む。
     *
     * [userPreferencesResultFlow] から取得した [UserPreferencesLoadResult] をもとに設定オブジェクトを生成する。
     *
     * @return カレンダー開始曜日設定 ([CalendarStartDayOfWeekPreference]) を放出するFlow。
     * @throws DataStoreReadException データストアへの読み込み(アクセス)に失敗した場合。([Flow] 内部で発生)
     * @throws DataNotFoundException 対応するデータが存在しない場合。([Flow] 内部で発生)
     */
    fun loadCalendarStartDayOfWeekPreference(): Flow<CalendarStartDayOfWeekPreference> {
        return userPreferencesResultFlow.map { result ->
            when (result) {
                is UserPreferencesLoadResult.Success -> createCalendarStartDayOfWeekPreference(result.preferences)
                is UserPreferencesLoadResult.Failure -> throw result.exception
            }
        }
    }

    /**
     * [Preferences] オブジェクトから [CalendarStartDayOfWeekPreference] を生成する。
     *
     * @param preferences DataStoreから読み込まれたPreferencesオブジェクト。
     * @return 生成された [CalendarStartDayOfWeekPreference]。
     * @throws DataNotFoundException 対応するキーのデータが存在しない場合。
     */
    private fun createCalendarStartDayOfWeekPreference(
        preferences: Preferences
    ): CalendarStartDayOfWeekPreference {
        val dayOfWeekNumber =
            preferences[calendarStartDayOfWeekPreferenceKey]
                ?: throw DataNotFoundException("カレンダー開始曜日")
        return CalendarStartDayOfWeekPreference(dayOfWeekNumber)
    }

    /**
     * リマインダー通知設定 ([ReminderNotificationPreference]) をFlowとして読み込む。
     *
     * [userPreferencesResultFlow] から取得した [UserPreferencesLoadResult] をもとに設定オブジェクトを生成する。
     *
     * @return リマインダー通知設定 ([ReminderNotificationPreference]) を放出するFlow。
     * @throws DataStoreReadException データストアへの読み込み(アクセス)に失敗した場合。([Flow] 内部で発生)
     * @throws DataNotFoundException 対応するデータが存在しない場合。([Flow] 内部で発生)
     */
    fun loadReminderNotificationPreference(): Flow<ReminderNotificationPreference> {
        return userPreferencesResultFlow.map { result ->
            when (result) {
                is UserPreferencesLoadResult.Success -> createReminderNotificationPreference(result.preferences)
                is UserPreferencesLoadResult.Failure -> throw result.exception
            }
        }
    }

    /**
     * [Preferences] オブジェクトから [ReminderNotificationPreference] を生成する。
     *
     * @param preferences DataStoreから読み込まれたPreferencesオブジェクト。
     * @return 生成された [ReminderNotificationPreference]。
     * @throws DataNotFoundException 対応するキーのデータが存在しない場合。
     */
    private fun createReminderNotificationPreference(
        preferences: Preferences
    ): ReminderNotificationPreference {
        val isEnabled =
            preferences[isEnabledReminderNotificationPreferenceKey]
        val notificationTimeString =
            preferences[reminderNotificationTimePreferenceKey]
        if (isEnabled == null || notificationTimeString == null) {
            throw DataNotFoundException("リマインダー通知")
        }
        return ReminderNotificationPreference(isEnabled, notificationTimeString)
    }

    /**
     * パスコードロック設定 ([PasscodeLockPreference]) をFlowとして読み込む。
     *
     * [userPreferencesResultFlow] から取得した [UserPreferencesLoadResult] をもとに設定オブジェクトを生成する。
     *
     * @return パスコードロック設定 ([PasscodeLockPreference]) を放出するFlow。
     * @throws DataStoreReadException データストアへの読み込み(アクセス)に失敗した場合。([Flow] 内部で発生)
     * @throws DataNotFoundException 対応するデータが存在しない場合。([Flow] 内部で発生)
     */
    fun loadPasscodeLockPreference(): Flow<PasscodeLockPreference> {
        return userPreferencesResultFlow.map { result ->
            when (result) {
                is UserPreferencesLoadResult.Success -> createPasscodeLockPreference(result.preferences)
                is UserPreferencesLoadResult.Failure -> throw result.exception
            }
        }
    }

    /**
     * [Preferences] オブジェクトから [PasscodeLockPreference] を生成する。
     *
     * @param preferences DataStoreから読み込まれたPreferencesオブジェクト。
     * @return 生成された [PasscodeLockPreference]。
     * @throws DataNotFoundException 対応するキーのデータが存在しない場合。
     */
    private fun createPasscodeLockPreference(preferences: Preferences): PasscodeLockPreference {
        val isEnabled = preferences[isEnabledPasscodeLockPreferenceKey]
        val passCode = preferences[passcodePreferenceKey]
        if (isEnabled == null || passCode == null) {
            throw DataNotFoundException("パスコードロック")
        }
        return PasscodeLockPreference(isEnabled, passCode)
    }

    /**
     * 天気情報取得設定 ([WeatherInfoFetchPreference]) をFlowとして読み込む。
     *
     * [userPreferencesResultFlow] から取得した [UserPreferencesLoadResult] をもとに設定オブジェクトを生成する。
     *
     * @return 天気情報取得設定 ([WeatherInfoFetchPreference]) を放出するFlow。
     * @throws DataStoreReadException データストアへの読み込み(アクセス)に失敗した場合。([Flow] 内部で発生)
     * @throws DataNotFoundException 対応するデータが存在しない場合。([Flow] 内部で発生)
     */
    fun loadWeatherInfoFetchPreference(): Flow<WeatherInfoFetchPreference> {
        return userPreferencesResultFlow.map { result ->
            when (result) {
                is UserPreferencesLoadResult.Success -> createWeatherInfoFetchPreference(result.preferences)
                is UserPreferencesLoadResult.Failure -> throw result.exception
            }
        }
    }

    /**
     * [Preferences] オブジェクトから [WeatherInfoFetchPreference] を生成する。
     *
     * @param preferences DataStoreから読み込まれたPreferencesオブジェクト。
     * @return 生成された [WeatherInfoFetchPreference]。
     * @throws DataNotFoundException 対応するキーのデータが存在しない場合。
     */
    private fun createWeatherInfoFetchPreference(
        preferences: Preferences
    ): WeatherInfoFetchPreference {
        val isEnabled = preferences[isEnabledWeatherInfoFetchPreferenceKey]
            ?: throw DataNotFoundException("天気情報取得")
        return WeatherInfoFetchPreference(isEnabled)
    }

    /**
     * テーマカラー設定 ([ThemeColorPreference]) を更新する。
     *
     * @param value 更新するテーマカラー設定。
     * @throws DataStoreWriteException データストアへの書き込みに失敗した場合。
     */
    suspend fun updateThemeColorPreference(value: ThemeColorPreference) {
        withContext(dispatcher) {
            executeDataStoreEditOperation { preferences ->
                preferences[themeColorPreferenceKey] = value.themeColorNumber
            }
        }
    }

    /**
     * カレンダーの開始曜日設定 ([CalendarStartDayOfWeekPreference]) を更新する。
     *
     * @param value 更新するカレンダー開始曜日設定。
     * @throws DataStoreWriteException データストアへの書き込みに失敗した場合。
     */
    suspend fun updateCalendarStartDayOfWeekPreference(value: CalendarStartDayOfWeekPreference) {
        withContext(dispatcher) {
            executeDataStoreEditOperation { preferences ->
                preferences[calendarStartDayOfWeekPreferenceKey] = value.dayOfWeekNumber
            }
        }
    }

    /**
     * リマインダー通知設定 ([ReminderNotificationPreference]) を更新する。
     *
     * @param value 更新するリマインダー通知設定。
     * @throws DataStoreWriteException データストアへの書き込みに失敗した場合。
     */
    suspend fun updateReminderNotificationPreference(value: ReminderNotificationPreference) {
        withContext(dispatcher) {
            executeDataStoreEditOperation { preferences ->
                preferences[isEnabledReminderNotificationPreferenceKey] = value.isEnabled
                preferences[reminderNotificationTimePreferenceKey] = value.notificationTimeString
            }
        }
    }

    /**
     * パスコードロック設定 ([PasscodeLockPreference]) を更新する。
     *
     * @param value 更新するパスコードロック設定。
     * @throws DataStoreWriteException データストアへの書き込みに失敗した場合。
     */
    suspend fun updatePasscodeLockPreference(value: PasscodeLockPreference) {
        withContext(dispatcher) {
            executeDataStoreEditOperation { preferences ->
                preferences[isEnabledPasscodeLockPreferenceKey] = value.isEnabled
                preferences[passcodePreferenceKey] = value.passcode
            }
        }
    }

    /**
     * 天気情報取得設定 ([WeatherInfoFetchPreference]) を更新する。
     *
     * @param value 更新する天気情報取得設定。
     * @throws DataStoreWriteException データストアへの書き込みに失敗した場合。
     */
    suspend fun updateWeatherInfoFetchPreference(value: WeatherInfoFetchPreference) {
        withContext(dispatcher) {
            executeDataStoreEditOperation { preferences ->
                preferences[isEnabledWeatherInfoFetchPreferenceKey] = value.isEnabled
            }
        }
    }

    /**
     * DataStoreの編集操作を実行するための共通ヘルパー関数。
     *
     * 指定された [operation] を [DataStore.edit] ブロック内で実行する。
     * [IOException] が発生した場合は、それをキャッチし、
     * [DataStoreWriteException] でラップして再スローする。
     *
     * @param operation [MutablePreferences] を引数に取り、DataStoreへの書き込み処理を行うsuspend関数。
     * @return DataStoreの編集操作後の [Preferences] オブジェクト。
     * @throws DataStoreWriteException データストアへの書き込みに失敗した場合。
     */
    private suspend fun executeDataStoreEditOperation(
        operation: suspend (MutablePreferences) -> Unit
    ): Preferences {
        return try {
            preferencesDataStore.edit { preferences ->
                operation(preferences)
            }
        } catch (e: IOException) {
            throw DataStoreWriteException(cause = e)
        }
    }
}
