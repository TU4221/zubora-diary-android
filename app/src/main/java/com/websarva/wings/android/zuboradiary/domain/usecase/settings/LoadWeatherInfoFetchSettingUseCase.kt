package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.WeatherInfoFetchSettingLoadException
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException
import com.websarva.wings.android.zuboradiary.domain.exception.UnknownException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * 天気情報取得設定を読み込むユースケース。
 *
 * 設定の読み込み結果を [Flow] として提供する。
 *
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 */
internal class LoadWeatherInfoFetchSettingUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "天気情報取得設定読込_"

    /**
     * ユースケースを実行し、天気情報取得設定の読み込み結果を [Flow] として返す。
     *
     * @return 読み込み結果を [UseCaseResult] へ [Flow] 内部でラップして返す。
     *   読み込みに成功した場合は[UseCaseResult.Success] に天気情報取得設定( [WeatherInfoFetchSetting] )を格納して返す。
     *   失敗した場合は、[UseCaseResult.Failure] にフォールバック値を格納した
     *   [WeatherInfoFetchSettingLoadException] を格納して返す。
     */
    operator fun invoke(): Flow<UseCaseResult<WeatherInfoFetchSetting, WeatherInfoFetchSettingLoadException>> {
        Log.i(logTag, "${logMsg}開始")

        return settingsRepository
            .loadWeatherInfoFetchSetting()
            .map { setting: WeatherInfoFetchSetting ->
                Log.d(
                    logTag,
                    "${logMsg}読込成功 (設定値: ${setting})"
                )
                val result: UseCaseResult<WeatherInfoFetchSetting, WeatherInfoFetchSettingLoadException> =
                    UseCaseResult.Success(setting)
                result
            }.catch { cause: Throwable ->
                val defaultSettingValue = WeatherInfoFetchSetting()
                val result =
                    when (cause) {
                        is ResourceNotFoundException -> {
                            Log.i(
                                logTag,
                                "${logMsg}_データ未発見_" +
                                        "デフォルト値を設定値として使用 (デフォルト値: $defaultSettingValue)"
                            )
                            UseCaseResult.Success(defaultSettingValue)
                        }
                        is UnknownException -> {
                            Log.w(
                                logTag,
                                "${logMsg}失敗_原因不明、" +
                                        "フォールバック値使用 (デフォルト値: $defaultSettingValue)",
                                cause
                            )
                            UseCaseResult.Failure(
                                WeatherInfoFetchSettingLoadException
                                    .Unknown(defaultSettingValue, cause),
                            )
                        }
                        is DomainException -> {
                            Log.w(
                                logTag,
                                "${logMsg}失敗_アクセス失敗、" +
                                        "フォールバック値使用 (デフォルト値: $defaultSettingValue)",
                                cause
                            )
                            UseCaseResult.Failure(
                                WeatherInfoFetchSettingLoadException
                                    .LoadFailure(defaultSettingValue, cause),
                            )
                        }
                        else -> throw cause
                    }
                emit(result)
            }
    }
}
