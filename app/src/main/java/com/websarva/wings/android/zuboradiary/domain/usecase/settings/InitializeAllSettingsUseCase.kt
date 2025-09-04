package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.CalendarStartDayOfWeekSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.PassCodeSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.ReminderNotificationSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.ThemeColorSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.exception.settings.WeatherInfoFetchSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
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
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 */
internal class InitializeAllSettingsUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "全設定初期化_"

    /**
     * ユースケースを実行し、全ての設定を初期値にリセットする。
     *
     * @return 初期化処理が成功した場合は [UseCaseResult.Success] を返す。
     *   処理中に [DomainException] が発生した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(): DefaultUseCaseResult<Unit> {
        Log.i(logTag, "${logMsg}開始")

        try {
            settingsRepository.saveThemeColorPreference(ThemeColorSetting())
            settingsRepository.saveCalendarStartDayOfWeekPreference(
                CalendarStartDayOfWeekSetting()
            )
            settingsRepository.saveReminderNotificationPreference(
                ReminderNotificationSetting.Disabled
            )
            settingsRepository.savePasscodeLockPreference(
                PasscodeLockSetting.Disabled
            )
            settingsRepository.saveWeatherInfoFetchPreference(
                WeatherInfoFetchSetting()
            )
        } catch (e: ThemeColorSettingUpdateFailureException) {
            Log.e(logTag, "${logMsg}失敗_テーマカラー設定保存エラー", e)
            return UseCaseResult.Failure(e)
        } catch (e: CalendarStartDayOfWeekSettingUpdateFailureException) {
            Log.e(logTag, "${logMsg}失敗_カレンダー開始曜日設定保存エラー", e)
            return UseCaseResult.Failure(e)
        } catch (e: ReminderNotificationSettingUpdateFailureException) {
            Log.e(logTag, "${logMsg}失敗_リマインダー通知設定保存エラー", e)
            return UseCaseResult.Failure(e)
        } catch (e: PassCodeSettingUpdateFailureException) {
            Log.e(logTag, "${logMsg}失敗_パスコードロック設定保存エラー", e)
            return UseCaseResult.Failure(e)
        } catch (e: WeatherInfoFetchSettingUpdateFailureException) {
            Log.e(logTag, "${logMsg}失敗_天気情報取得設定保存エラー", e)
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
