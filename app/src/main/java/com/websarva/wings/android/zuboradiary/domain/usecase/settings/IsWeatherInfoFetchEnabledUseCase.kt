package com.websarva.wings.android.zuboradiary.domain.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.preferences.AllPreferences
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class IsWeatherInfoFetchEnabledUseCase(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(): UseCaseResult.Success<Boolean> {
        val logMsg = "天気情報取得設定確認_"
        Log.i(logTag, "${logMsg}開始")

        // TODO:first()処理方法をプロジェクトで統一する。
        val value =
            withContext(Dispatchers.IO) {
                userPreferencesRepository
                    .loadAllPreferences()
                    .map { value: AllPreferences ->
                        value.weatherInfoFetchPreference.isChecked
                    }.first()
            }
        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(value)
    }
}
