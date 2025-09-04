package com.websarva.wings.android.zuboradiary.domain.repository

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.domain.exception.settings.CalendarStartDayOfWeekSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.PassCodeSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.ReminderNotificationSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.ThemeColorSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.WeatherInfoFetchSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingDataSourceResult
import kotlinx.coroutines.flow.Flow

/**
 * アプリケーション設定値へのアクセスと永続化を抽象化するリポジトリインターフェース。
 *
 * このインターフェースは、各種アプリケーション設定の読み込みと保存機能を提供します。
 * 各メソッドは、操作に失敗した場合にドメイン固有の例外 ([DomainException] のサブクラス) をスローします。
 */
internal interface SettingsRepository {

    // TODO:例外のエラーハンドリングをDomain層で処理するように変更(現在データ層で処理)。例外も作成。
    /**
     * テーマカラー設定を読み込む。
     *
     * @return テーマカラー設定 ([ThemeColorSetting]) を内包した結果 ([UserSettingDataSourceResult]) を放出する Flow。
     * @throws ThemeColorSettingLoadFailureException 設定の読み込みに失敗した場合。
    *   ([Flow] 内部で発生する可能性がある)
     */
    fun loadThemeColorPreference(): Flow<UserSettingDataSourceResult<ThemeColorSetting>>

    /**
     * カレンダーの開始曜日設定を読み込む。
     *
     * @return カレンダー開始曜日設定 ([CalendarStartDayOfWeekSetting]) を内包した結果 ([UserSettingDataSourceResult]) を放出する Flow。
     * @throws CalendarStartDayOfWeekSettingLoadFailureException 設定の読み込みに失敗した場合。
     *   ([Flow] 内部で発生する可能性がある)
     */
    fun loadCalendarStartDayOfWeekPreference():
            Flow<UserSettingDataSourceResult<CalendarStartDayOfWeekSetting>>

    /**
     * リマインダー通知設定を読み込む。
     *
     * @return リマインダー通知設定 ([ReminderNotificationSetting]) を内包した結果 ([UserSettingDataSourceResult]) を放出する Flow。
     * @throws ReminderNotificationSettingLoadFailureException 設定の読み込みに失敗した場合。
     *   ([Flow] 内部で発生する可能性がある)
     */
    fun loadReminderNotificationPreference():
            Flow<UserSettingDataSourceResult<ReminderNotificationSetting>>

    /**
     * パスコードロック設定を読み込む。
     *
     * @return パスコードロック設定 ([PasscodeLockSetting]) を内包した結果 ([UserSettingDataSourceResult]) を放出する Flow。
     * @throws PassCodeSettingLoadFailureException 設定の読み込みに失敗した場合。
     *   ([Flow] 内部で発生する可能性がある)
     */
    fun loadPasscodeLockPreference():
            Flow<UserSettingDataSourceResult<PasscodeLockSetting>>

    /**
     * 天気情報取得設定を読み込む。
     *
     * @return 天気情報取得設定 ([WeatherInfoFetchSetting]) を内包した結果 ([UserSettingDataSourceResult]) を放出する Flow。
     * @throws WeatherInfoFetchSettingLoadFailureException 設定の読み込みに失敗した場合。
     *   ([Flow] 内部で発生する可能性がある)
     */
    fun loadWeatherInfoFetchPreference():
            Flow<UserSettingDataSourceResult<WeatherInfoFetchSetting>>

    /**
     * テーマカラー設定を保存する。
     *
     * @param setting 保存するテーマカラー設定。
     * @throws ThemeColorSettingUpdateFailureException テーマカラー設定の保存に失敗した場合。
     */
    suspend fun saveThemeColorPreference(setting: ThemeColorSetting)

    /**
     * カレンダーの開始曜日設定を保存する。
     *
     * @param setting 保存するカレンダー開始曜日設定。
     * @throws CalendarStartDayOfWeekSettingUpdateFailureException カレンダー開始曜日設定の保存に失敗した場合。
     */
    suspend fun saveCalendarStartDayOfWeekPreference(setting: CalendarStartDayOfWeekSetting)

    /**
     * リマインダー通知設定を保存する。
     *
     * @param setting 保存するリマインダー通知設定。
     * @throws ReminderNotificationSettingUpdateFailureException リマインダー通知設定の保存に失敗した場合。
     */
    suspend fun saveReminderNotificationPreference(setting: ReminderNotificationSetting)

    /**
     * パスコードロック設定を保存する。
     *
     * @param setting 保存するパスコードロック設定。
     * @throws PassCodeSettingUpdateFailureException パスコードロック設定の保存に失敗した場合。
     */
    suspend fun savePasscodeLockPreference(setting: PasscodeLockSetting)

    /**
     * 天気情報取得設定を保存する。
     *
     * @param setting 保存する天気情報取得設定。
     * @throws WeatherInfoFetchSettingUpdateFailureException 天気情報取得設定の保存に失敗した場合。
     */
    suspend fun saveWeatherInfoFetchPreference(setting: WeatherInfoFetchSetting)
}
