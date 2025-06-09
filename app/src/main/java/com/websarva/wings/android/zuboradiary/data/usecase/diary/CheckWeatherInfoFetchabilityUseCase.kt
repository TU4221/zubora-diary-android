package com.websarva.wings.android.zuboradiary.data.usecase.diary

import com.websarva.wings.android.zuboradiary.data.model.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.WeatherApiRepository
import java.time.LocalDate

internal class CheckWeatherInfoFetchabilityUseCase(
    private val weatherApiRepository: WeatherApiRepository
) {

    operator fun invoke(date: LocalDate): UseCaseResult<Boolean> {
        return try {
            UseCaseResult.Success(weatherApiRepository.canFetchWeatherInfo(date))
        } catch (e: Exception) {
            UseCaseResult.Error(e)
        }
    }
}
