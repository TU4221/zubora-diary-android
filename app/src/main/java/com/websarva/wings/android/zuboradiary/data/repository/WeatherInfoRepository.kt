package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.location.FusedLocationAccessFailureException
import com.websarva.wings.android.zuboradiary.data.location.FusedLocationDataSource
import com.websarva.wings.android.zuboradiary.data.mapper.weather.toDomainModel
import com.websarva.wings.android.zuboradiary.domain.model.Weather
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiDataSource
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiException
import com.websarva.wings.android.zuboradiary.domain.exception.weather.WeatherInfoFetchException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

internal class WeatherInfoRepository (
    private val weatherApiDataSource: WeatherApiDataSource,
    private val fusedLocationDataSource: FusedLocationDataSource
) {

    fun canFetchWeatherInfo(date: LocalDate): Boolean {
        return weatherApiDataSource.canFetchWeatherInfo(date)
    }

    @Throws(WeatherInfoFetchException::class)
    suspend fun fetchWeatherInfo(date: LocalDate): Weather {
        return withContext(Dispatchers.IO) {
            try {
                val location = fusedLocationDataSource.fetchCurrentLocation()
                weatherApiDataSource
                    .fetchWeatherInfo(date, location.latitude, location.longitude)
                    .toDomainModel()
            } catch (e: FusedLocationAccessFailureException) {
                throw WeatherInfoFetchException.AccessLocationFailure(e)
            } catch (e: WeatherApiException) {
                when (e) {
                    is WeatherApiException.ApiAccessFailure ->
                        throw WeatherInfoFetchException.ApiAccessFailure(date, e)
                    is WeatherApiException.DateOutOfRange ->
                        throw WeatherInfoFetchException.DateOutOfRange(date, e)
                }
            }
        }
    }
}
