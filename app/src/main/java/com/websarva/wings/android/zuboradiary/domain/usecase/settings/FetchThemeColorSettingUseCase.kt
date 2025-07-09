package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.domain.model.settings.UserSettingFlowResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.flow.Flow

internal class FetchThemeColorSettingUseCase(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val logTag = createLogTag()

    operator fun invoke(): UseCaseResult.Success<Flow<UserSettingFlowResult<ThemeColorSetting>>> {
        val logMsg = "テーマカラー設定取得_"
        Log.i(logTag, "${logMsg}開始")

        val value = userPreferencesRepository.fetchThemeColorPreference()

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(value)
    }
}
