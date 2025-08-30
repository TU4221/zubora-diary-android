package com.websarva.wings.android.zuboradiary.data.network

import android.util.Log
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.datastore.core.IOException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import retrofit2.Response
import java.net.ConnectException
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.net.ssl.SSLException

internal class WeatherApiDataSource(private val weatherApiService: WeatherApiService) {

    private val logTag = createLogTag()

    // MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
    //      その為、@Suppress("RedundantSuppression")で警告回避。
    @Suppress("unused", "RedundantSuppression") // MEMO:@IntRangeで使用する為、@Suppressで警告回避。
    companion object {
        const val MIN_PAST_DAYS = 1 //過去天気情報取得可能最小日
        const val MAX_PAST_DAYS = 92 //過去天気情報取得可能最大日
    }

    private val queryDailyParameter = "weather_code"
    private val queryTimeZoneParameter = "Asia/Tokyo"

    @Throws(WeatherApiException::class)
    suspend fun fetchWeatherInfo(
        date: LocalDate,
        @FloatRange(from = -90.0, to = 90.0)
        latitude: Double,
        @FloatRange(from = -180.0, to = 180.0)
        longitude: Double
    ): WeatherApiData {
        require(latitude >= -90)
        require(latitude <= 90)
        require(longitude >= -180)
        require(longitude <= 180)

        if (!canFetchWeatherInfo(date)) throw WeatherApiException.DateOutOfRange(date)

        val currentDate = LocalDate.now()
        return if (date == currentDate) {
            fetchTodayWeatherInfo(latitude, longitude)
        } else {
            val betweenDays = ChronoUnit.DAYS.between(date, currentDate).toInt()
            fetchPastDayWeatherInfo(latitude, longitude, betweenDays)
        }
    }

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

    @Throws(WeatherApiException.ApiAccessFailure::class)
    suspend fun fetchTodayWeatherInfo(
        @FloatRange(from = -90.0, to = 90.0)
        latitude: Double,
        @FloatRange(from = -180.0, to = 180.0)
        longitude: Double
    ): WeatherApiData {
        val response =
            executeWebApiOperation {
                weatherApiService.getWeather(
                    latitude.toString(),
                    longitude.toString(),
                    queryDailyParameter,
                    queryTimeZoneParameter,
                    "0",  /*today*/
                    "1" /*1日分*/
                )
            }
        return toWeatherApiData(response)
    }

    @Throws(WeatherApiException.ApiAccessFailure::class)
    suspend fun fetchPastDayWeatherInfo(
        @FloatRange(from = -90.0, to = 90.0)
        latitude: Double,
        @FloatRange(from = -180.0, to = 180.0)
        longitude: Double,
        @IntRange(from = MIN_PAST_DAYS.toLong(), to = MAX_PAST_DAYS.toLong())
        numPastDays: Int
    ): WeatherApiData {
        require(numPastDays >= MIN_PAST_DAYS)
        require(numPastDays <= MAX_PAST_DAYS)

        val response =
            executeWebApiOperation {
                weatherApiService.getWeather(
                    latitude.toString(),
                    longitude.toString(),
                    queryDailyParameter,
                    queryTimeZoneParameter,
                    numPastDays.toString(),
                    "0" /*1日分(過去日から1日分取得する場合"0"を代入)*/
                )
            }
        return toWeatherApiData(response)
    }

    @Throws(WeatherApiException.ApiAccessFailure::class)
    private fun toWeatherApiData(response: Response<WeatherApiData>): WeatherApiData {
        Log.d(logTag, "code = " + response.code())
        Log.d(logTag, "message = :" + response.message())

        return if (response.isSuccessful) {
            Log.d(logTag, "body = " + response.body())
            val result =
                response.body()
                    ?: throw WeatherApiException.ApiAccessFailure(IOException())
            result
        } else {
            // HTTPエラー (4xx, 5xx)
            response.errorBody().use { errorBody ->
                val errorBodyString = errorBody?.string() ?: "null"
                Log.d(
                    logTag,
                    "errorBody = $errorBodyString"
                )
            }
            throw WeatherApiException.ApiAccessFailure(IOException())
        }
    }

    @Throws(WeatherApiException.ApiAccessFailure::class)
    private suspend fun <R> executeWebApiOperation(
        operation: suspend () -> R
    ): R {
        return try {
            operation()
        } catch (e: UnknownHostException) {
            // DNS解決に失敗した場合 (例: インターネット接続なし、ホスト名間違い)
            throw WeatherApiException.ApiAccessFailure(e)
        } catch (e: ConnectException) {
            // サーバーへのTCP接続に失敗した場合 (例: サーバーダウン、ポートが開いていない)
            throw WeatherApiException.ApiAccessFailure(e)
        } catch (e: java.net.SocketTimeoutException) {
            // 接続または読み取りタイムアウト
            throw WeatherApiException.ApiAccessFailure(e)
        } catch (e: SSLException) {
            // SSL/TLS ハンドシェイクエラー
            throw WeatherApiException.ApiAccessFailure(e)
        } catch (e: IOException) {
            // 上記以外の一般的なI/Oエラー (例: 予期せぬ接続切断など)
            throw WeatherApiException.ApiAccessFailure(e)
        }
    }
}
