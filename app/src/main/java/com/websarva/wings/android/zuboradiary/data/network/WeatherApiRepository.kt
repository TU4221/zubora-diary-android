package com.websarva.wings.android.zuboradiary.data.network

import android.util.Log
import androidx.annotation.IntRange
import retrofit2.Response
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class WeatherApiRepository @Inject constructor(private val weatherApiService: WeatherApiService) {

    @Suppress("unused") // MEMO:@IntRangeで使用する為、@Suppressで警告回避。
    companion object {
        private const val MIN_PAST_DAYS = 1 //過去天気情報取得可能最小日
        private const val MAX_PAST_DAYS = 92 //過去天気情報取得可能最大日
    }

    private val queryDiaryParameter = "weather_code"
    private val queryTimeZoneParameter = "Asia/Tokyo"


    fun canFetchWeatherInfo(date: LocalDate): Boolean {
        val currentDate = LocalDate.now()
        Log.d("fetchWeatherInfo", "isAfter:" + date.isAfter(currentDate))
        if (date.isAfter(currentDate)) return false

        val betweenDays = ChronoUnit.DAYS.between(date, currentDate)
        Log.d("fetchWeatherInfo", "betweenDays:$betweenDays")

        return betweenDays <= MAX_PAST_DAYS
    }

    suspend fun fetchTodayWeatherInfo(geoCoordinates: GeoCoordinates): Response<WeatherApiData> {
        return weatherApiService.getWeather(
            geoCoordinates.latitude.toString(),
            geoCoordinates.longitude.toString(),
            queryDiaryParameter,
            queryTimeZoneParameter,
            "0",  /*today*/
            "1" /*1日分*/
        )
    }

    suspend fun fetchPastDayWeatherInfo(
        geoCoordinates: GeoCoordinates,
        @IntRange(from = MIN_PAST_DAYS.toLong(), to = MAX_PAST_DAYS.toLong())
        numPastDays: Int
    ): Response<WeatherApiData> {
        require(numPastDays >= MIN_PAST_DAYS)
        require(numPastDays <= MAX_PAST_DAYS)

        return weatherApiService.getWeather(
            geoCoordinates.latitude.toString(),
            geoCoordinates.longitude.toString(),
            queryDiaryParameter,
            queryTimeZoneParameter,
            numPastDays.toString(),
            "0" /*1日分(過去日から1日分取得する場合"0"を代入)*/
        )
    }
}
