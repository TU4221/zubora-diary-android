package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.preferences.AllPreferences
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow

internal class FetchAllSettingsValueUseCase(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val logTag = createLogTag()

    operator fun invoke(): UseCaseResult.Success<Flow<AllPreferences>> {
        val logMsg = "全設定値取得_"
        Log.i(logTag, "${logMsg}開始")

        val value = userPreferencesRepository.loadAllPreferences()

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(value)
    }
}
