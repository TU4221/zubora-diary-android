package com.websarva.wings.android.zuboradiary.data.network

import android.util.Log
import androidx.annotation.IntRange
import com.websarva.wings.android.zuboradiary.getLogTag
import retrofit2.Response
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal class WeatherApiRepository (private val weatherApiService: WeatherApiService) {

    // MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
    //      その為、@Suppress("RedundantSuppression")で警告回避。
    @Suppress("unused", "RedundantSuppression") // MEMO:@IntRangeで使用する為、@Suppressで警告回避。
    companion object {
        private const val MIN_PAST_DAYS = 1 //過去天気情報取得可能最小日
        private const val MAX_PAST_DAYS = 92 //過去天気情報取得可能最大日
    }

    private val logTag = getLogTag()

    private val queryDiaryParameter = "weather_code"
    private val queryTimeZoneParameter = "Asia/Tokyo"


    fun canFetchWeatherInfo(date: LocalDate): Boolean {
        val currentDate = LocalDate.now()

        if (date.isAfter(currentDate)) {
            Log.d(logTag, "canFetchWeatherInfo(date = $date) = false")
            return false
        }

        val betweenDays = ChronoUnit.DAYS.between(date, currentDate)
        val result = betweenDays <= MAX_PAST_DAYS
        Log.d(logTag, "canFetchWeatherInfo(date = $date) = $result")
        return result
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
