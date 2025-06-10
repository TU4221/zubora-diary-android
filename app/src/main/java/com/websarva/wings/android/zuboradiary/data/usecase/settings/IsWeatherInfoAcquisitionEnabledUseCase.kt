package com.websarva.wings.android.zuboradiary.data.usecase.settings

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.model.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.preferences.AllPreferences
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class IsWeatherInfoAcquisitionEnabledUseCase(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val logTag = createLogTag()

    suspend operator fun invoke(): UseCaseResult<Boolean> {
        val logMsg = "天気情報取得設定値取得"
        Log.i(logTag, "${logMsg}_開始")
        return try {
            // TODO:first()処理方法をプロジェクトで統一する。
            val value =
                withContext(Dispatchers.IO) {
                    userPreferencesRepository
                        .loadAllPreferences()
                        .map { value: AllPreferences ->
                            value.weatherInfoAcquisitionPreference.isChecked
                        }.first()
                }
            Log.i(logTag, "${logMsg}_完了")
            UseCaseResult.Success(value)
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}_失敗", e)
            UseCaseResult.Error(e)
        }
    }
}
