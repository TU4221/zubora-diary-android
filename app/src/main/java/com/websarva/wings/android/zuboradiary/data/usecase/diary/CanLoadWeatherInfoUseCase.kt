package com.websarva.wings.android.zuboradiary.data.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.WeatherApiRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class CanLoadWeatherInfoUseCase(
    private val weatherApiRepository: WeatherApiRepository
) {

    private val logTag = createLogTag()

    operator fun invoke(date: LocalDate): UseCaseResult<Boolean, Nothing> {
        val logMsg = "天気情報取得可能日確認_"
        Log.i(logTag, "${logMsg}開始")
        val canFetch = weatherApiRepository.canFetchWeatherInfo(date)
        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(canFetch)
    }
}
