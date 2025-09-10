package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.ThemeColorSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
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
     * @param themeColor 更新するテーマカラー。
     * @return 更新処理が成功した場合は [UseCaseResult.Success] を返す。
     *   更新処理中に [DataStorageException] が発生した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        themeColor: ThemeColor
    ): UseCaseResult<Unit, ThemeColorSettingUpdateException> {
        Log.i(logTag, "${logMsg}開始 (テーマカラー: $themeColor)")

        try {
            val preferenceValue = ThemeColorSetting(themeColor)
            settingsRepository.updateThemeColorPreference(preferenceValue)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_設定更新処理エラー", e)
            return UseCaseResult.Failure(
                ThemeColorSettingUpdateException.UpdateFailure(themeColor, e)
            )
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
