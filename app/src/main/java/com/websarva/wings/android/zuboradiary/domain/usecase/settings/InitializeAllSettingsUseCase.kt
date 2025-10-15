package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.CalendarStartDayOfWeekSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.PassCodeSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.ReminderNotificationSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.AllSettingsInitializationException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.ThemeColorSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.WeatherInfoFetchSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.utils.logTag

/**
 * アプリケーションの全ての設定項目を初期値にリセットするユースケース。
 *
 * 具体的には以下の設定を初期化する。
 * - テーマカラー
 * - カレンダーの週の開始曜日
 * - リマインダー通知
 * - パスコードロック
 * - 天気情報取得
 *
 * @property updateThemeColorSettingUseCase テーマカラー設定を更新するユースケース。
 * @property updateCalendarStartDayOfWeekSettingUseCase カレンダーの週の開始曜日設定を更新するユースケース。
 * @property updateReminderNotificationSettingUseCase リマインダー通知設定を更新するユースケース。
 * @property updatePasscodeLockSettingUseCase パスコードロック設定を更新するユースケース。
 * @property updateWeatherInfoFetchSettingUseCase 天気情報取得設定を更新するユースケース。
 *
 */
internal class InitializeAllSettingsUseCase(
    private val updateThemeColorSettingUseCase: UpdateThemeColorSettingUseCase,
    private val updateCalendarStartDayOfWeekSettingUseCase: UpdateCalendarStartDayOfWeekSettingUseCase,
    private val updateReminderNotificationSettingUseCase: UpdateReminderNotificationSettingUseCase,
    private val updatePasscodeLockSettingUseCase: UpdatePasscodeLockSettingUseCase,
    private val updateWeatherInfoFetchSettingUseCase: UpdateWeatherInfoFetchSettingUseCase
) {

    private val logMsg = "全設定初期化_"

    /**
     * ユースケースを実行し、全ての設定を初期値にリセットする。
     *
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [AllSettingsInitializationException] を格納して返す。
     */
    suspend operator fun invoke(): UseCaseResult<Unit, AllSettingsInitializationException> {
        Log.i(logTag, "${logMsg}開始")

        return try {
            initializeThemeColorSetting()
            initializeCalendarStartDayOfWeekSetting()
            initializeReminderNotificationSetting()
            initializePasscodeLockSetting()
            initializeWeatherInfoFetchSetting()
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(Unit)
        } catch (e: ThemeColorSettingUpdateException) {
            when (e) {
                is ThemeColorSettingUpdateException.UpdateFailure -> {
                    Log.e(logTag, "${logMsg}失敗_テーマカラー設定保存エラー", e)
                    UseCaseResult.Failure(
                        AllSettingsInitializationException.InitializationFailure(e)
                    )
                }
                is ThemeColorSettingUpdateException.InsufficientStorage -> {
                    Log.e(logTag, "${logMsg}失敗_ストレージ容量不足", e)
                    UseCaseResult.Failure(
                        AllSettingsInitializationException.InsufficientStorage(e)
                    )
                }
                is ThemeColorSettingUpdateException.Unknown -> {
                    Log.e(logTag, "${logMsg}失敗_原因不明", e)
                    UseCaseResult.Failure(
                        AllSettingsInitializationException.Unknown(e)
                    )
                }
            }
        } catch (e: CalendarStartDayOfWeekSettingUpdateException) {
            when (e) {
                is CalendarStartDayOfWeekSettingUpdateException.UpdateFailure -> {
                    Log.e(logTag, "${logMsg}失敗_カレンダー開始曜日設定保存エラー", e)
                    UseCaseResult.Failure(
                        AllSettingsInitializationException.InitializationFailure(e)
                    )
                }
                is CalendarStartDayOfWeekSettingUpdateException.InsufficientStorage -> {
                    Log.e(logTag, "${logMsg}失敗_ストレージ容量不足", e)
                    UseCaseResult.Failure(
                        AllSettingsInitializationException.InsufficientStorage(e)
                    )
                }
                is CalendarStartDayOfWeekSettingUpdateException.Unknown -> {
                    Log.e(logTag, "${logMsg}失敗_原因不明", e)
                    UseCaseResult.Failure(
                        AllSettingsInitializationException.Unknown(e)
                    )
                }
            }
        } catch (e: ReminderNotificationSettingUpdateException) {
            when (e) {
                is ReminderNotificationSettingUpdateException.SettingUpdateFailure -> {
                    Log.e(logTag, "${logMsg}失敗_リマインダー通知設定保存エラー", e)
                    UseCaseResult.Failure(
                        AllSettingsInitializationException.InitializationFailure(e)
                    )
                }
                is ReminderNotificationSettingUpdateException.InsufficientStorage -> {
                    Log.e(logTag, "${logMsg}失敗_ストレージ容量不足", e)
                    UseCaseResult.Failure(
                        AllSettingsInitializationException.InsufficientStorage(e)
                    )
                }
                is ReminderNotificationSettingUpdateException.Unknown -> {
                    Log.e(logTag, "${logMsg}失敗_原因不明", e)
                    UseCaseResult.Failure(
                        AllSettingsInitializationException.Unknown(e)
                    )
                }
            }
        } catch (e: PassCodeSettingUpdateException) {
            when (e) {
                is PassCodeSettingUpdateException.UpdateFailure -> {
                    Log.e(logTag, "${logMsg}失敗_パスコードロック設定保存エラー", e)
                    UseCaseResult.Failure(
                        AllSettingsInitializationException.InitializationFailure(e)
                    )
                }
                is PassCodeSettingUpdateException.InsufficientStorage -> {
                    Log.e(logTag, "${logMsg}失敗_ストレージ容量不足", e)
                    UseCaseResult.Failure(
                        AllSettingsInitializationException.InsufficientStorage(e)
                    )
                }
                is PassCodeSettingUpdateException.Unknown -> {
                    Log.e(logTag, "${logMsg}失敗_原因不明", e)
                    UseCaseResult.Failure(
                        AllSettingsInitializationException.Unknown(e)
                    )
                }
            }
        } catch (e: WeatherInfoFetchSettingUpdateException) {
            when (e) {
                is WeatherInfoFetchSettingUpdateException.UpdateFailure -> {
                    Log.e(logTag, "${logMsg}失敗_天気情報取得設定保存エラー", e)
                    UseCaseResult.Failure(
                        AllSettingsInitializationException.InitializationFailure(e)
                    )
                }
                is WeatherInfoFetchSettingUpdateException.InsufficientStorage -> {
                    Log.e(logTag, "${logMsg}失敗_ストレージ容量不足", e)
                    UseCaseResult.Failure(
                        AllSettingsInitializationException.InsufficientStorage(e)
                    )
                }
                is WeatherInfoFetchSettingUpdateException.Unknown -> {
                    Log.e(logTag, "${logMsg}失敗_原因不明", e)
                    UseCaseResult.Failure(
                        AllSettingsInitializationException.Unknown(e)
                    )
                }
            }
        }
    }

    /**
     * テーマカラー設定を初期化する。
     *
     * @throws ThemeColorSettingUpdateException テーマカラー設定の初期化に失敗した場合。
     */
    private suspend fun initializeThemeColorSetting() {
        when (val result = updateThemeColorSettingUseCase(ThemeColorSetting.default())) {
            is UseCaseResult.Success -> { /*処理なし*/ }
            is UseCaseResult.Failure -> throw result.exception
        }
    }

    /**
     * カレンダーの週の開始曜日設定を初期化する。
     *
     * @throws CalendarStartDayOfWeekSettingUpdateException カレンダーの週の開始曜日設定の初期化に失敗した場合。
     */
    private suspend fun initializeCalendarStartDayOfWeekSetting() {
        when (val result = updateCalendarStartDayOfWeekSettingUseCase(CalendarStartDayOfWeekSetting.default())) {
            is UseCaseResult.Success -> { /*処理なし*/ }
            is UseCaseResult.Failure -> throw result.exception
        }
    }

    /**
     * リマインダー通知設定を初期化する。
     *
     * @throws ReminderNotificationSettingUpdateException リマインダー通知設定の初期化に失敗した場合。
     */
    private suspend fun initializeReminderNotificationSetting() {
        when (val result = updateReminderNotificationSettingUseCase(ReminderNotificationSetting.default())) {
            is UseCaseResult.Success -> { /*処理なし*/ }
            is UseCaseResult.Failure -> throw result.exception
        }
    }

    /**
     * パスコードロック設定を初期化する。
     *
     * @throws PassCodeSettingUpdateException パスコードロック設定の初期化に失敗した場合。
     */
    private suspend fun initializePasscodeLockSetting() {
        when (val result = updatePasscodeLockSettingUseCase(PasscodeLockSetting.default())) {
            is UseCaseResult.Success -> { /*処理なし*/ }
            is UseCaseResult.Failure -> throw result.exception
        }
    }

    /**
     * 天気情報取得設定を初期化する。
     *
     * @throws WeatherInfoFetchSettingUpdateException 天気情報取得設定の初期化に失敗した場合。
     */
    private suspend fun initializeWeatherInfoFetchSetting() {
        when (val result = updateWeatherInfoFetchSettingUseCase(WeatherInfoFetchSetting.default())) {
            is UseCaseResult.Success -> { /*処理なし*/ }
            is UseCaseResult.Failure -> throw result.exception
        }
    }
}
