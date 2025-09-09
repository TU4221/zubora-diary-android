package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.exception.settings.ThemeColorSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * テーマカラー設定を保存するユースケース。
 *
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 */
internal class SaveThemeColorSettingUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "テーマカラー設定保存_"

    /**
     * ユースケースを実行し、指定されたテーマカラーを保存する。
     *
     * @param themeColor 保存するテーマカラー。
     * @return 保存処理が成功した場合は [UseCaseResult.Success] を返す。
     *   保存処理中に [DataStorageException] が発生した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        themeColor: ThemeColor
    ): UseCaseResult<Unit, ThemeColorSettingUpdateException> {
        Log.i(logTag, "${logMsg}開始 (テーマカラー: $themeColor)")

        try {
            val preferenceValue = ThemeColorSetting(themeColor)
            settingsRepository.saveThemeColorPreference(preferenceValue)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_設定保存処理エラー", e)
            return UseCaseResult.Failure(
                ThemeColorSettingUpdateException.UpdateFailure(themeColor, e)
            )
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
