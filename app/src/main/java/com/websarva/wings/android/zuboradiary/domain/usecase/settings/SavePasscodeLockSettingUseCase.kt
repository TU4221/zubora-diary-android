package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.data.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.exception.settings.PassCodeSettingUpdateFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class SavePasscodeLockSettingUseCase(
    private val settingsRepository: SettingsRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        isChecked: Boolean,
        passcode: String = ""
    ): DefaultUseCaseResult<Unit> {
        val logMsg = "パスコードロック設定保存_"
        Log.i(logTag, "${logMsg}開始")

        try {
            val preferenceValue =
                if (isChecked) {
                    PasscodeLockSetting.Enabled(passcode)
                } else {
                    PasscodeLockSetting.Disabled
                }
            settingsRepository.savePasscodeLockPreference(preferenceValue)
        } catch (e: PassCodeSettingUpdateFailureException) {
            Log.e(logTag, "${logMsg}失敗")
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
