package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.ThemeColorSettingLoadException
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingResult
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.NotFoundException
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
     * リポジトリから設定値を読み込み、その結果を [UserSettingResult] に変換する。
     * - リポジトリからの読み込み成功時: [UserSettingResult.Success] を発行。
     * - リポジトリからの読み込み失敗時 ([DataStorageException]、 [NotFoundException] が発生した場合):
     *     - [DataStorageException]: [UserSettingResult.Failure] を発行し、デフォルト値をフォールバックとして提供する。
     *     - [NotFoundException]: [UserSettingResult.Success] を発行し、デフォルト値を設定値として提供する。
     *
     * @return テーマカラー設定の読み込み結果を [UserSettingResult] へ [Flow] でラップし、
     *   [UseCaseResult.Success] に格納して返す。このユースケースは、常に [UseCaseResult.Success] を返す。
     */
    operator fun invoke(): UseCaseResult.Success<Flow<UserSettingResult<ThemeColorSetting>>> {
        Log.i(logTag, "${logMsg}開始")

        val flow =
            settingsRepository
                .loadThemeColorSetting()
                .map { setting: ThemeColorSetting ->
                    Log.d(
                        logTag,
                        "${logMsg}読込成功 (設定値: ${setting})"
                    )
                    val result: UserSettingResult<ThemeColorSetting> =
                        UserSettingResult.Success(setting)
                    result
                }.catch { cause: Throwable ->
                    val defaultSettingValue = ThemeColorSetting()
                    val userSettingResult =
                        when (cause) {
                            is DataStorageException -> {
                                Log.w(
                                    logTag,
                                    "${logMsg}失敗_アクセス失敗、" +
                                            "フォールバック値使用 (デフォルト値: $defaultSettingValue)",
                                    cause
                                )
                                UserSettingResult.Failure(
                                    ThemeColorSettingLoadException.LoadFailure(cause),
                                    defaultSettingValue
                                )
                            }
                            is NotFoundException -> {
                                Log.i(
                                    logTag,
                                    "${logMsg}失敗_データ未発見、" +
                                            "デフォルト値を設定値として使用 (デフォルト値: $defaultSettingValue)"
                                )
                                UserSettingResult.Success(defaultSettingValue)
                            }
                            else -> throw cause
                        }
                    emit(userSettingResult)
                }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(flow)
    }
}
