package com.websarva.wings.android.zuboradiary.data.network

import android.util.Log
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.datastore.core.IOException
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.ConnectException
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.net.ssl.SSLException

/**
 * 天気情報APIへのデータアクセスを行うデータソースクラス。
 *
 * このクラスは、[WeatherApiService] を使用してOpen-Meteo APIから天気情報を取得する。
 * APIへのアクセス失敗等で発生する特定の例外を[WeatherApiException] にラップする。
 *
 * @property weatherApiService Retrofitサービスインターフェースのインスタンス。
 * @property dispatcher 天気情報の取得を実行するスレッドプール。
 */
internal class WeatherApiDataSource(
    private val weatherApiService: WeatherApiService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val logTag = createLogTag()

    // MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
    //      その為、@Suppress("RedundantSuppression")で警告回避。
    @Suppress("unused", "RedundantSuppression") // MEMO:@IntRangeで使用する為、@Suppressで警告回避。
    companion object {
        /** 過去天気情報取得可能最小日数。 */
        const val MIN_PAST_DAYS = 1
        /** 過去天気情報取得可能最大日数。 */
        const val MAX_PAST_DAYS = 92
    }

    private val queryDailyParameter = "weather_code"
    private val queryTimeZoneParameter = "Asia/Tokyo"

    /**
     * 指定された日付、緯度、経度に基づいて天気情報を取得する。
     *
     * 現在日付の場合は当日の天気情報を、過去日の場合は過去の天気情報を取得する。
     *
     * @param date 天気情報を取得する日付。
     * @param latitude 天気情報を取得する地点の緯度 (-90.0 から 90.0 の範囲)。
     * @param longitude 天気情報を取得する地点の経度 (-180.0 から 180.0 の範囲)。
     * @return 取得した天気情報データ。
     * @throws WeatherApiException APIアクセスに失敗した場合、または日付が範囲外の場合。
     */
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
        return withContext(dispatcher) {
            if (date == currentDate) {
                fetchTodayWeatherInfo(latitude, longitude)
            } else {
                val betweenDays = ChronoUnit.DAYS.between(date, currentDate).toInt()
                fetchPastDayWeatherInfo(latitude, longitude, betweenDays)
            }
        }
    }

    /**
     * 指定された日付の天気情報が取得可能かどうかを判定する。
     *
     * 未来の日付、または[MAX_PAST_DAYS]で定義された最大過去日数より前の日付は取得不可とする。
     *
     * @param date 判定する日付。
     * @return 取得可能であればtrue、そうでなければfalse。
     */
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

    /**
     * 今日の天気情報を取得する。
     *
     * @param latitude 天気情報を取得する地点の緯度 (-90.0 から 90.0 の範囲)。
     * @param longitude 天気情報を取得する地点の経度 (-180.0 から 180.0 の範囲)。
     * @return 取得した今日の天気情報データ ([WeatherApiData])。
     * @throws WeatherApiException.ApiAccessFailure APIアクセスに失敗した場合。
     */
    private suspend fun fetchTodayWeatherInfo(
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

    /**
     * 指定された過去日数の天気情報を取得する。
     *
     * @param latitude 天気情報を取得する地点の緯度 (-90.0 から 90.0 の範囲)。
     * @param longitude 天気情報を取得する地点の経度 (-180.0 から 180.0 の範囲)。
     * @param numPastDays 何日前の天気情報を取得するか ([MIN_PAST_DAYS] から [MAX_PAST_DAYS] の範囲)。
     * @return 取得した過去の天気情報データ。
     * @throws WeatherApiException.ApiAccessFailure APIアクセスに失敗した場合。
     * @throws IllegalArgumentException numPastDaysが不正な範囲の場合。
     */
    private suspend fun fetchPastDayWeatherInfo(
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

    /**
     * Retrofitの[Response]を[WeatherApiData]に変換する。
     *
     * レスポンスが成功し、かつボディが存在する場合はそれを返す。
     * それ以外の場合は[WeatherApiException.ApiAccessFailure]をスローする。
     *
     * @param response Retrofitからのレスポンスオブジェクト。
     * @return 変換された[WeatherApiData]。
     * @throws WeatherApiException.ApiAccessFailure レスポンスが不成功またはボディがnullの場合。
     */
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

    /**
     * Web API操作を実行し、一般的なネットワーク関連の例外をラップする。
     *
     * [UnknownHostException] (DNS解決失敗など)、
     * [ConnectException] (サーバー接続失敗など)、
     * [java.net.SocketTimeoutException] (タイムアウト)、
     * [SSLException] (SSL/TLSハンドシェイクエラー)、
     * またはその他の [java.io.IOException] (予期せぬ接続切断など) が発生した場合、
     * それを [WeatherApiException.ApiAccessFailure] でラップして再スローする。
     *
     * @param R 操作の結果の型。
     * @param operation 実行するsuspend関数形式のWeb API操作。
     * @return Web API操作の結果。
     * @throws WeatherApiException.ApiAccessFailure Web APIアクセスに失敗した場合。
     */
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
