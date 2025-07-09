package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferenceFlowResult
import com.websarva.wings.android.zuboradiary.data.preferences.WeatherInfoFetchPreference
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class IsWeatherInfoFetchEnabledUseCase(
    private val fetchWeatherInfoFetchSettingUseCase: FetchWeatherInfoFetchSettingUseCase
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(): UseCaseResult.Success<Boolean> {
        val logMsg = "天気情報取得設定確認_"
        Log.i(logTag, "${logMsg}開始")

        // TODO:first()処理方法をプロジェクトで統一する。
        val value =
            withContext(Dispatchers.IO) {
                fetchWeatherInfoFetchSettingUseCase().value
                    .map { value: UserPreferenceFlowResult<WeatherInfoFetchPreference> ->
                         when (value) {
                            is UserPreferenceFlowResult.Success -> {
                                value.preference.isChecked
                            }
                            is UserPreferenceFlowResult.Failure -> {
                                value.fallbackPreference.isChecked
                            }
                         }
                    }.first()
            }
        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(value)
    }
}
