package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 天気情報取得設定が有効かどうかを確認するユースケース。
 *
 * @property loadWeatherInfoFetchSettingUseCase 天気情報取得設定を読み込むユースケース。
 */
internal class CheckWeatherInfoFetchEnabledUseCase @Inject constructor(
    private val loadWeatherInfoFetchSettingUseCase: LoadWeatherInfoFetchSettingUseCase
) {

    private val logMsg = "天気情報取得設定確認_"

    /**
     * ユースケースを実行し、天気情報取得設定が有効かどうかを返す。
     *
     * 設定の読み込みに成功した場合はその設定値を、失敗した場合はフォールバック設定値を参照する。
     *
     * @return 天気情報取得設定が有効な場合は `true`、無効な場合は `false` を
     *   [UseCaseResult.Success] に格納して返す。
     *   このユースケースは常に成功するため、[UseCaseResult.Failure] を返すことはない。
     */
    suspend operator fun invoke(): UseCaseResult.Success<Boolean> {
        Log.i(logTag, "${logMsg}開始")

        val value =
            withContext(Dispatchers.IO) {
                loadWeatherInfoFetchSettingUseCase()
                    .map { result ->
                        when (result) {
                            is UseCaseResult.Success -> {
                                result.value.isEnabled
                            }
                            is UseCaseResult.Failure -> {
                                Log.w(
                                    logTag, "${logMsg}設定読み込み失敗、" +
                                        "フォールバック値を使用 (有効: ${result.exception.fallbackSetting.isEnabled})",
                                    result.exception
                                )
                                result.exception.fallbackSetting.isEnabled
                            }
                        }
                    }.first()
            }
        Log.i(logTag, "${logMsg}完了_結果: $value")
        return UseCaseResult.Success(value)
    }
}
