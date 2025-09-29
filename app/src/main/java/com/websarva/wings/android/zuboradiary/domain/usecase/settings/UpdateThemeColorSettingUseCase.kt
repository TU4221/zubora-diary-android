package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.ThemeColorSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * テーマカラー設定を更新するユースケース。
 *
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 */
internal class UpdateThemeColorSettingUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "テーマカラー設定更新_"

    /**
     * ユースケースを実行し、指定されたテーマカラーを更新する。
     *
     * @param setting 更新する設定 [ThemeColorSetting] 。
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [ThemeColorSettingUpdateException] を格納して返す。
     */
    suspend operator fun invoke(
        setting: ThemeColorSetting
    ): UseCaseResult<Unit, ThemeColorSettingUpdateException> {
        Log.i(logTag, "${logMsg}開始 (設定値: $setting)")

        return try {
            settingsRepository.updateThemeColorSetting(setting)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(Unit)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_設定更新エラー", e)
            return UseCaseResult.Failure(
                ThemeColorSettingUpdateException.UpdateFailure(setting, e)
            )
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}失敗_原因不明", e)
            UseCaseResult.Failure(
                ThemeColorSettingUpdateException.Unknown(e)
            )
        }
    }
}
