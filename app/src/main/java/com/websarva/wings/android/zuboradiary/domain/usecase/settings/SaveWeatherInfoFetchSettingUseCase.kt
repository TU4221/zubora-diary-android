package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.model.settings.WeatherInfoFetchSetting
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.domain.exception.settings.UpdatePassCodeSettingFailedException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal class SaveWeatherInfoFetchSettingUseCase(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(
        isChecked: Boolean
    ): DefaultUseCaseResult<Unit> {
        val logMsg = "天気情報取得設定保存_"
        Log.i(logTag, "${logMsg}開始")

        try {
            val preferenceValue = WeatherInfoFetchSetting(isChecked)
            userPreferencesRepository.saveWeatherInfoFetchPreference(preferenceValue)
        } catch (e: UpdatePassCodeSettingFailedException) {
            Log.e(logTag, "${logMsg}失敗")
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
