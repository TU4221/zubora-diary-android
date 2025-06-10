package com.websarva.wings.android.zuboradiary.data.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.model.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.WeatherApiRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class CanFetchWeatherInfoUseCase(
    private val weatherApiRepository: WeatherApiRepository
) {

    private val logTag = createLogTag()

    operator fun invoke(date: LocalDate): UseCaseResult<Boolean> {
        val logMsg = "天気情報取得可能日確認_"
        Log.i(logTag, "${logMsg}開始")
        return try {
            val canFetch = weatherApiRepository.canFetchWeatherInfo(date)
            Log.i(logTag, "${logMsg}完了")
            UseCaseResult.Success(canFetch)
        } catch (e: Exception) {
            Log.e(logTag, "${logMsg}失敗")
            UseCaseResult.Error(e)
        }
    }
}
