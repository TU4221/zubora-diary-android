package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.exception.settings.WeatherInfoFetchSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 天気情報取得設定を保存するユースケース。
 *
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 */
internal class SaveWeatherInfoFetchSettingUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "天気情報取得設定保存_"

    /**
     * ユースケースを実行し、天気情報取得設定を保存する。
     *
     * @param isChecked 天気情報取得を有効にする場合は `true`、無効にする場合は `false`。
     * @return 保存処理が成功した場合は [UseCaseResult.Success] を返す。
     *   保存処理中に [DataStorageException] が発生した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        isChecked: Boolean
    ): UseCaseResult<Unit, WeatherInfoFetchSettingUpdateException> {
        Log.i(logTag, "${logMsg}開始 (有効: $isChecked)")

        try {
            val preferenceValue = WeatherInfoFetchSetting(isChecked)
            settingsRepository.saveWeatherInfoFetchPreference(preferenceValue)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_設定保存処理エラー", e)
            return UseCaseResult.Failure(
                WeatherInfoFetchSettingUpdateException.UpdateFailure(isChecked, e)
            )
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
