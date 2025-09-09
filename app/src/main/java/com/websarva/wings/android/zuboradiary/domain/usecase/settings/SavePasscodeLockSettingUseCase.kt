package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.exception.settings.PassCodeSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * パスコードロック設定を保存するユースケース。
 *
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 */
internal class SavePasscodeLockSettingUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "パスコードロック設定保存_"

    /**
     * ユースケースを実行し、パスコードロック設定を保存する。
     *
     * @param isChecked パスコードロックを有効にする場合は `true`、無効にする場合は `false`。
     * @param passcode 設定するパスコード。`isChecked` が `true` の場合のみ使用される。
     * @return 保存処理が成功した場合は [UseCaseResult.Success] を返す。
     *   保存処理中に [DataStorageException] が発生した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        isChecked: Boolean,
        passcode: String = ""
    ): UseCaseResult<Unit, PassCodeSettingUpdateException> {
        Log.i(logTag, "${logMsg}開始 (有効: $isChecked, パスコード: ${if (passcode.isNotEmpty()) "設定あり" else "設定なし"})")

        try {
            val preferenceValue =
                if (isChecked) {
                    PasscodeLockSetting.Enabled(passcode)
                } else {
                    PasscodeLockSetting.Disabled
                }
            settingsRepository.savePasscodeLockPreference(preferenceValue)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_設定保存処理エラー", e)
            return UseCaseResult.Failure(
                PassCodeSettingUpdateException.UpdateFailure(isChecked, e)
            )
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
