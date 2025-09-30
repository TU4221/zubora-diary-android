package com.websarva.wings.android.zuboradiary.domain.repository

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException
import kotlinx.coroutines.flow.Flow

/**
 * アプリケーション設定値へのアクセスと永続化を抽象化するリポジトリインターフェース。
 *
 * このインターフェースは、各種アプリケーション設定の読み込みと更新機能を提供します。
 * 各メソッドは、操作に失敗した場合にドメイン固有の例外 ([UseCaseException] のサブクラス) をスローします。
 */
internal interface SettingsRepository {

    /**
     * テーマカラー設定を読み込む。
     *
     * @return テーマカラー設定 ([ThemeColorSetting]) を放出するFlow。
     * @throws DataStorageException 設定の読み込みに失敗した場合。([Flow] 内部で発生する可能性がある)
     * @throws ResourceNotFoundException 保存された設定値が見つからなかった場合。([Flow] 内部で発生する可能性がある)
     */
    fun loadThemeColorSetting(): Flow<ThemeColorSetting>

    /**
     * カレンダーの開始曜日設定を読み込む。
     *
     * @return カレンダー開始曜日設定 ([CalendarStartDayOfWeekSetting]) を放出するFlow。
     * @throws DataStorageException 設定の読み込みに失敗した場合。([Flow] 内部で発生する可能性がある)
     * @throws ResourceNotFoundException 保存された設定値が見つからなかった場合。([Flow] 内部で発生する可能性がある)
     */
    fun loadCalendarStartDayOfWeekSetting(): Flow<CalendarStartDayOfWeekSetting>

    /**
     * リマインダー通知設定を読み込む。
     *
     * @return リマインダー通知設定 ([ReminderNotificationSetting]) を放出するFlow。
     * @throws DataStorageException 設定の読み込みに失敗した場合。([Flow] 内部で発生する可能性がある)
     * @throws ResourceNotFoundException 保存された設定値が見つからなかった場合。([Flow] 内部で発生する可能性がある)
     */
    fun loadReminderNotificationSetting(): Flow<ReminderNotificationSetting>

    /**
     * パスコードロック設定を読み込む。
     *
     * @return パスコードロック設定 ([PasscodeLockSetting]) を放出するFlow。
     * @throws DataStorageException 設定の読み込みに失敗した場合。([Flow] 内部で発生する可能性がある)
     * @throws ResourceNotFoundException 保存された設定値が見つからなかった場合。([Flow] 内部で発生する可能性がある)
     */
    fun loadPasscodeLockSetting(): Flow<PasscodeLockSetting>

    /**
     * 天気情報取得設定を読み込む。
     *
     * @return 天気情報取得設定 ([WeatherInfoFetchSetting]) を放出するFlow。
     * @throws DataStorageException 設定の読み込みに失敗した場合。([Flow] 内部で発生する可能性がある)
     * @throws ResourceNotFoundException 保存された設定値が見つからなかった場合。([Flow] 内部で発生する可能性がある)
     */
    fun loadWeatherInfoFetchSetting(): Flow<WeatherInfoFetchSetting>

    /**
     * テーマカラー設定を更新する。
     *
     * @param setting 更新するテーマカラー設定。
     * @throws DataStorageException テーマカラー設定の更新に失敗した場合。
     */
    suspend fun updateThemeColorSetting(setting: ThemeColorSetting)

    /**
     * カレンダーの開始曜日設定を更新する。
     *
     * @param setting 更新するカレンダー開始曜日設定。
     * @throws DataStorageException カレンダー開始曜日設定の更新に失敗した場合。
     */
    suspend fun updateCalendarStartDayOfWeekSetting(setting: CalendarStartDayOfWeekSetting)

    /**
     * リマインダー通知設定を更新する。
     *
     * @param setting 更新するリマインダー通知設定。
     * @throws DataStorageException リマインダー通知設定の更新に失敗した場合。
     */
    suspend fun updateReminderNotificationSetting(setting: ReminderNotificationSetting)

    /**
     * パスコードロック設定を更新する。
     *
     * @param setting 更新するパスコードロック設定。
     * @throws DataStorageException パスコードロック設定の更新に失敗した場合。
     */
    suspend fun updatePasscodeLockSetting(setting: PasscodeLockSetting)

    /**
     * 天気情報取得設定を更新する。
     *
     * @param setting 更新する天気情報取得設定。
     * @throws DataStorageException 天気情報取得設定の更新に失敗した場合。
     */
    suspend fun updateWeatherInfoFetchSetting(setting: WeatherInfoFetchSetting)
}
