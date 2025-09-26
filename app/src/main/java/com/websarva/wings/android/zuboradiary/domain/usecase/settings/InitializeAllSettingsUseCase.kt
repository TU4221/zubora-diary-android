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
import com.websarva.wings.android.zuboradiary.utils.createLogTag

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

    private val logTag = createLogTag()
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
            updateThemeColorSettingUseCase(ThemeColorSetting())
            updateCalendarStartDayOfWeekSettingUseCase(CalendarStartDayOfWeekSetting())
            updateReminderNotificationSettingUseCase(ReminderNotificationSetting.Disabled)
            updatePasscodeLockSettingUseCase(PasscodeLockSetting.Disabled)
            updateWeatherInfoFetchSettingUseCase(WeatherInfoFetchSetting())
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(Unit)
        } catch (e: ThemeColorSettingUpdateException) {
            Log.e(logTag, "${logMsg}失敗_テーマカラー設定保存エラー", e)
            UseCaseResult.Failure(
                AllSettingsInitializationException.ThemeColorInitializationFailure(e)
            )
        } catch (e: CalendarStartDayOfWeekSettingUpdateException) {
            Log.e(logTag, "${logMsg}失敗_カレンダー開始曜日設定保存エラー", e)
            UseCaseResult.Failure(
                AllSettingsInitializationException.CalendarStartDayOfWeeksInitializationFailure(e)
            )
        } catch (e: ReminderNotificationSettingUpdateException) {
            Log.e(logTag, "${logMsg}失敗_リマインダー通知設定保存エラー", e)
            UseCaseResult.Failure(
                AllSettingsInitializationException.ReminderNotificationInitializationFailure(e)
            )
        } catch (e: PassCodeSettingUpdateException) {
            Log.e(logTag, "${logMsg}失敗_パスコードロック設定保存エラー", e)
            UseCaseResult.Failure(
                AllSettingsInitializationException.PasscodeInitializationFailure(e)
            )
        } catch (e: WeatherInfoFetchSettingUpdateException) {
            Log.e(logTag, "${logMsg}失敗_天気情報取得設定保存エラー", e)
            UseCaseResult.Failure(
                AllSettingsInitializationException.WeatherInfoFetchInitializationFailure(e)
            )
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            UseCaseResult.Failure(
                AllSettingsInitializationException.Unknown(e)
            )
        }
    }
}
