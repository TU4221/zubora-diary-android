package com.websarva.wings.android.zuboradiary.domain.repository

import com.websarva.wings.android.zuboradiary.domain.exception.weather.WeatherInfoFetchException
import com.websarva.wings.android.zuboradiary.domain.model.Weather
import java.time.LocalDate

internal interface WeatherInfoRepository {

    fun canFetchWeatherInfo(date: LocalDate): Boolean

    @Throws(WeatherInfoFetchException::class)
    suspend fun fetchWeatherInfo(date: LocalDate): Weather
}
