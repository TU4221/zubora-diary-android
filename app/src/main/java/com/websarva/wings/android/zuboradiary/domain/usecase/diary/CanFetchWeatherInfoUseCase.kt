package com.websarva.wings.android.zuboradiary.domain.usecase.diary

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.WeatherInfoRepository
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalDate

internal class CanFetchWeatherInfoUseCase(
    private val weatherInfoRepository: WeatherInfoRepository
) {

    private val logTag = createLogTag()

    operator fun invoke(date: LocalDate): UseCaseResult.Success<Boolean> {
        val logMsg = "天気情報取得可能日確認_"
        Log.i(logTag, "${logMsg}開始")
        val canFetch = weatherInfoRepository.canFetchWeatherInfo(date)
        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(canFetch)
    }
}
