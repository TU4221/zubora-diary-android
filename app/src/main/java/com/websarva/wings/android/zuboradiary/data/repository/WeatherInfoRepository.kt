package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.model.GeoCoordinates
import com.websarva.wings.android.zuboradiary.data.model.Weather
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiDataSource
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiException
import com.websarva.wings.android.zuboradiary.domain.exception.weather.FetchWeatherInfoException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

internal class WeatherInfoRepository (private val weatherApiDataSource: WeatherApiDataSource) {

    fun canFetchWeatherInfo(date: LocalDate): Boolean {
        return weatherApiDataSource.canFetchWeatherInfo(date)
    }

    @Throws(FetchWeatherInfoException::class)
    suspend fun fetchWeatherInfo(date: LocalDate, geoCoordinates: GeoCoordinates): Weather {
        return withContext(Dispatchers.IO) {
            try {
                weatherApiDataSource.fetchWeatherInfo(date, geoCoordinates)
            } catch (e: WeatherApiException) {
                when (e) {
                    is WeatherApiException.ApiAccessFailed ->
                        throw FetchWeatherInfoException.ApiAccessFailed(date, e)
                    is WeatherApiException.DateOutOfRange ->
                        throw FetchWeatherInfoException.DateOutOfRange(date, e)
                }
            }
        }
    }
}
