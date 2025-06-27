package com.websarva.wings.android.zuboradiary.data.repository

import androidx.annotation.IntRange
import com.websarva.wings.android.zuboradiary.data.model.GeoCoordinates
import com.websarva.wings.android.zuboradiary.data.model.Weather
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiAccessException
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiDataSource
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiDataSource.Companion.MAX_PAST_DAYS
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiDataSource.Companion.MIN_PAST_DAYS
import com.websarva.wings.android.zuboradiary.domain.exception.weather.FetchWeatherInfoFailedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

internal class WeatherInfoRepository (private val weatherApiDataSource: WeatherApiDataSource) {

    fun canFetchWeatherInfo(date: LocalDate): Boolean {
        return weatherApiDataSource.canFetchWeatherInfo(date)
    }

    @Throws(FetchWeatherInfoFailedException::class)
    suspend fun fetchTodayWeatherInfo(geoCoordinates: GeoCoordinates): Weather {
        return withContext(Dispatchers.IO) {
            try {
                weatherApiDataSource.fetchTodayWeatherInfo(geoCoordinates)
            } catch (e: WeatherApiAccessException) {
                throw FetchWeatherInfoFailedException(e)
            }
        }
    }

    @Throws(FetchWeatherInfoFailedException::class)
    suspend fun fetchPastDayWeatherInfo(
        geoCoordinates: GeoCoordinates,
        @IntRange(from = MIN_PAST_DAYS.toLong(), to = MAX_PAST_DAYS.toLong())
        numPastDays: Int
    ): Weather {
        return withContext(Dispatchers.IO) {
            try {
                weatherApiDataSource.fetchPastDayWeatherInfo(geoCoordinates, numPastDays)
            } catch (e: WeatherApiAccessException) {
                throw FetchWeatherInfoFailedException(e)
            }
        }
    }
}
