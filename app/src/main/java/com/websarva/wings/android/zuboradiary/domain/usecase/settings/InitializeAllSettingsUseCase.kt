package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.CalendarStartDayOfWeekSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.PassCodeSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.ReminderNotificationSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.AllSettingsInitializationException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.ThemeColorSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.WeatherInfoFetchSettingUpdateException
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
 * @property saveThemeColorSettingUseCase テーマカラー設定を更新するユースケース。
 * @property saveCalendarStartDayOfWeekSettingUseCase カレンダーの週の開始曜日設定を更新するユースケース。
 * @property saveReminderNotificationSettingUseCase リマインダー通知設定を更新するユースケース。
 * @property savePasscodeLockSettingUseCase パスコードロック設定を更新するユースケース。
 * @property saveWeatherInfoFetchSettingUseCase 天気情報取得設定を更新するユースケース。
 *
 */
internal class InitializeAllSettingsUseCase(
    private val saveThemeColorSettingUseCase: SaveThemeColorSettingUseCase,
    private val saveCalendarStartDayOfWeekSettingUseCase: SaveCalendarStartDayOfWeekUseCase,
    private val saveReminderNotificationSettingUseCase: SaveReminderNotificationSettingUseCase,
    private val savePasscodeLockSettingUseCase: SavePasscodeLockSettingUseCase,
    private val saveWeatherInfoFetchSettingUseCase: SaveWeatherInfoFetchSettingUseCase
) {

    private val logTag = createLogTag()
    private val logMsg = "全設定初期化_"

    /**
     * ユースケースを実行し、全ての設定を初期値にリセットする。
     *
     * @return 初期化処理が成功した場合は [UseCaseResult.Success] を返す。
     *   処理中に [UseCaseException] が発生した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(): UseCaseResult<Unit, AllSettingsInitializationException> {
        Log.i(logTag, "${logMsg}開始")

        try {
            saveThemeColorSettingUseCase(ThemeColorSetting().themeColor)
            saveCalendarStartDayOfWeekSettingUseCase(CalendarStartDayOfWeekSetting().dayOfWeek)
            saveReminderNotificationSettingUseCase(ReminderNotificationSetting.Disabled.isEnabled)
            savePasscodeLockSettingUseCase(PasscodeLockSetting.Disabled.isEnabled)
            saveWeatherInfoFetchSettingUseCase(WeatherInfoFetchSetting().isEnabled)
        } catch (e: ThemeColorSettingUpdateException) {
            Log.e(logTag, "${logMsg}失敗_テーマカラー設定保存エラー", e)
            return UseCaseResult.Failure(
                AllSettingsInitializationException.ThemeColorInitializationFailure(e)
            )
        } catch (e: CalendarStartDayOfWeekSettingUpdateException) {
            Log.e(logTag, "${logMsg}失敗_カレンダー開始曜日設定保存エラー", e)
            return UseCaseResult.Failure(
                AllSettingsInitializationException.CalendarStartDayOfWeeksInitializationFailure(e)
            )
        } catch (e: ReminderNotificationSettingUpdateException) {
            Log.e(logTag, "${logMsg}失敗_リマインダー通知設定保存エラー", e)
            return UseCaseResult.Failure(
                AllSettingsInitializationException.ReminderNotificationInitializationFailure(e)
            )
        } catch (e: PassCodeSettingUpdateException) {
            Log.e(logTag, "${logMsg}失敗_パスコードロック設定保存エラー", e)
            return UseCaseResult.Failure(
                AllSettingsInitializationException.PasscodeInitializationFailure(e)
            )
        } catch (e: WeatherInfoFetchSettingUpdateException) {
            Log.e(logTag, "${logMsg}失敗_天気情報取得設定保存エラー", e)
            return UseCaseResult.Failure(
                AllSettingsInitializationException.WeatherInfoFetchInitializationFailure(e)
            )
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
