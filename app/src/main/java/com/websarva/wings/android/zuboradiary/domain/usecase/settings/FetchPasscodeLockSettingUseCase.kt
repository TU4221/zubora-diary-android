package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.preferences.PassCodeLockPreference
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferenceFlowResult
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow

internal class FetchPasscodeLockSettingUseCase(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val logTag = createLogTag()

    operator fun invoke(): UseCaseResult.Success<Flow<UserPreferenceFlowResult<PassCodeLockPreference>>> {
        val logMsg = "パスコードロック設定取得_"
        Log.i(logTag, "${logMsg}開始")

        val value = userPreferencesRepository.fetchPasscodeLockPreference()

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(value)
    }
}
