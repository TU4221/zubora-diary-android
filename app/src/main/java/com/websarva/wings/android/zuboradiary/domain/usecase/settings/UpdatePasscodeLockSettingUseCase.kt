package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception.PassCodeSettingUpdateException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.DataStorageException
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * パスコードロック設定を更新するユースケース。
 *
 * @property settingsRepository 設定関連の操作を行うリポジトリ。
 */
internal class UpdatePasscodeLockSettingUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "パスコードロック設定更新_"

    /**
     * ユースケースを実行し、パスコードロック設定を更新する。
     *
     * @param setting 更新する設定 [PasscodeLockSetting] オブジェクト。
     * @return 更新処理が成功した場合は [UseCaseResult.Success] を返す。
     *   更新処理中に [DataStorageException] が発生した場合は [UseCaseResult.Failure] を返す。
     */
    suspend operator fun invoke(
        setting: PasscodeLockSetting
    ): UseCaseResult<Unit, PassCodeSettingUpdateException> {
        Log.i(logTag, "${logMsg}開始 (有効: ${setting.isEnabled}")

        try {
            settingsRepository.updatePasscodeLockSetting(setting)
        } catch (e: DataStorageException) {
            Log.e(logTag, "${logMsg}失敗_設定更新処理エラー", e)
            return UseCaseResult.Failure(
                PassCodeSettingUpdateException.UpdateFailure(setting, e)
            )
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
