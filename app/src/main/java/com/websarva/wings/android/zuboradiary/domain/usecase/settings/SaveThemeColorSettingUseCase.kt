package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColorPreference
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdateThemeColorSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class SaveThemeColorSettingUseCase(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        themeColor: ThemeColor
    ): DefaultUseCaseResult<Unit> {
        val logMsg = "テーマカラー設定保存_"
        Log.i(logTag, "${logMsg}開始")

        try {
            val preferenceValue = ThemeColorPreference(themeColor)
            userPreferencesRepository.saveThemeColorPreference(preferenceValue)
        } catch (e: UpdateThemeColorSettingFailedException) {
            Log.e(logTag, "${logMsg}失敗")
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
