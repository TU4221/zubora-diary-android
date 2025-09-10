package com.websarva.wings.android.zuboradiary.data.repository

import com.websarva.wings.android.zuboradiary.data.mapper.weather.toDomainModel
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiDataSource
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiException
import com.websarva.wings.android.zuboradiary.domain.model.SimpleLocation
import com.websarva.wings.android.zuboradiary.domain.model.Weather
import com.websarva.wings.android.zuboradiary.domain.repository.WeatherInfoRepository
import com.websarva.wings.android.zuboradiary.domain.repository.exception.InvalidParameterException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.NetworkConnectionException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

internal class WeatherInfoRepositoryImpl (
    private val weatherApiDataSource: WeatherApiDataSource
) : WeatherInfoRepository {

    override fun canFetchWeatherInfo(date: LocalDate): Boolean {
        return weatherApiDataSource.canFetchWeatherInfo(date)
    }

    override suspend fun fetchWeatherInfo(date: LocalDate, location: SimpleLocation): Weather {
        return withContext(Dispatchers.IO) {
            try {
                weatherApiDataSource
                    .fetchWeatherInfo(date, location.latitude, location.longitude)
                    .toDomainModel()
            } catch (e: WeatherApiException) {
                when (e) {
                    is WeatherApiException.ApiAccessFailure ->
                        throw NetworkConnectionException(cause = e)
                    is WeatherApiException.DateOutOfRange ->
                        throw InvalidParameterException(cause = e)
                }
            }
        }
    }
}
