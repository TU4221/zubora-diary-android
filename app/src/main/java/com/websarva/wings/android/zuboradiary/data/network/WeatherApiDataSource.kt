package com.websarva.wings.android.zuboradiary.data.network

import android.util.Log
import androidx.annotation.IntRange
import androidx.datastore.core.IOException
import com.websarva.wings.android.zuboradiary.data.model.GeoCoordinates
import com.websarva.wings.android.zuboradiary.data.model.Weather
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import retrofit2.Response
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal class WeatherApiDataSource(private val weatherApiService: WeatherApiService) {

    private val logTag = createLogTag()

    // MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
    //      その為、@Suppress("RedundantSuppression")で警告回避。
    @Suppress("unused", "RedundantSuppression") // MEMO:@IntRangeで使用する為、@Suppressで警告回避。
    companion object {
        const val MIN_PAST_DAYS = 1 //過去天気情報取得可能最小日
        const val MAX_PAST_DAYS = 92 //過去天気情報取得可能最大日
    }

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

    suspend fun fetchTodayWeatherInfo(geoCoordinates: GeoCoordinates): Weather {
        val response =
            weatherApiService.getWeather(
                geoCoordinates.latitude.toString(),
                geoCoordinates.longitude.toString(),
                queryDiaryParameter,
                queryTimeZoneParameter,
                "0",  /*today*/
                "1" /*1日分*/
            )
        return toWeatherInfo(response)
    }

    suspend fun fetchPastDayWeatherInfo(
        geoCoordinates: GeoCoordinates,
        @IntRange(from = MIN_PAST_DAYS.toLong(), to = MAX_PAST_DAYS.toLong())
        numPastDays: Int
    ): Weather {
        require(numPastDays >= MIN_PAST_DAYS)
        require(numPastDays <= MAX_PAST_DAYS)

        val response =
            weatherApiService.getWeather(
                geoCoordinates.latitude.toString(),
                geoCoordinates.longitude.toString(),
                queryDiaryParameter,
                queryTimeZoneParameter,
                numPastDays.toString(),
                "0" /*1日分(過去日から1日分取得する場合"0"を代入)*/
            )
        return toWeatherInfo(response)
    }

    private fun toWeatherInfo(response: Response<WeatherApiData>): Weather {
        Log.d(logTag, "code = " + response.code())
        Log.d(logTag, "message = :" + response.message())

        return if (response.isSuccessful) {
            Log.d(logTag, "body = " + response.body())
            val result =
                response.body()?.toWeatherInfo() ?: throw IllegalStateException()
            result
        } else {
            response.errorBody().use { errorBody ->
                val errorBodyString = errorBody?.string() ?: "null"
                Log.d(
                    logTag,
                    "errorBody = $errorBodyString"
                )
            }
            throw IOException()
        }
    }
}
