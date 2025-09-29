package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.WeatherInfoFetchSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 天気情報取得設定を更新するユースケース。
 *
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 */
internal class UpdateWeatherInfoFetchSettingUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "天気情報取得設定更新_"

    /**
     * ユースケースを実行し、天気情報取得設定を更新する。
     *
     * @param setting 更新する設定 [WeatherInfoFetchSetting] 。
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [WeatherInfoFetchSettingUpdateException] を格納して返す。
     */
    suspend operator fun invoke(
        setting: WeatherInfoFetchSetting
    ): UseCaseResult<Unit, WeatherInfoFetchSettingUpdateException> {
        Log.i(logTag, "${logMsg}開始 (設定値: $setting)")

        return try {
            settingsRepository.updateWeatherInfoFetchSetting(setting)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(Unit)
        } catch (e: DomainException) {
            Log.e(logTag, "${logMsg}失敗_設定更新エラー", e)
            UseCaseResult.Failure(
                WeatherInfoFetchSettingUpdateException.UpdateFailure(setting, e)
            )
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            UseCaseResult.Failure(
                WeatherInfoFetchSettingUpdateException.Unknown(e)
            )
        }
    }
}
