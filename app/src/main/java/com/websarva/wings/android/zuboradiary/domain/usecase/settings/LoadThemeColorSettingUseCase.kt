package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.ThemeColorSettingLoadException
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.ResourceNotFoundException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * テーマカラー設定を読み込むユースケース。
 *
 * 設定の読み込み結果を [Flow] として提供する。
 *
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 */
internal class LoadThemeColorSettingUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "テーマカラー設定読込_"

    /**
     * ユースケースを実行し、テーマカラー設定の読み込み結果を [Flow] として返す。
     *
     * @return 読み込み結果を [UseCaseResult] へ [Flow] 内部でラップして返す。
     *   読み込みに成功した場合は[UseCaseResult.Success] にテーマカラー設定( [ThemeColorSetting] )を格納して返す。
     *   失敗した場合は、[UseCaseResult.Failure] にフォールバック値を格納した
     *   [ThemeColorSettingLoadException] を格納して返す。
     */
    operator fun invoke(): Flow<UseCaseResult<ThemeColorSetting, ThemeColorSettingLoadException>> {
        Log.i(logTag, "${logMsg}開始")

        return settingsRepository
            .loadThemeColorSetting()
            .map { setting: ThemeColorSetting ->
                Log.d(
                    logTag,
                    "${logMsg}読込成功 (設定値: ${setting})"
                )
                val result: UseCaseResult<ThemeColorSetting, ThemeColorSettingLoadException> =
                    UseCaseResult.Success(setting)
                result
            }.catch { cause: Throwable ->
                val defaultSettingValue = ThemeColorSetting()
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
                        is DomainException -> {
                            Log.w(
                                logTag,
                                "${logMsg}失敗_アクセス失敗、" +
                                        "フォールバック値使用 (デフォルト値: $defaultSettingValue)",
                                cause
                            )
                            UseCaseResult.Failure(
                                ThemeColorSettingLoadException
                                    .LoadFailure(defaultSettingValue, cause),
                            )
                        }
                        else -> {
                            Log.w(
                                logTag,
                                "${logMsg}失敗_原因不明、" +
                                        "フォールバック値使用 (デフォルト値: $defaultSettingValue)",
                                cause
                            )
                            UseCaseResult.Failure(
                                ThemeColorSettingLoadException
                                    .Unknown(defaultSettingValue, cause),
                            )
                        }
                    }
                emit(result)
            }
    }
}
