package com.websarva.wings.android.zuboradiary.data.network

import android.util.Log
import androidx.annotation.IntRange
import androidx.datastore.core.IOException
import com.websarva.wings.android.zuboradiary.data.network.exception.HttpException
import com.websarva.wings.android.zuboradiary.data.network.exception.NetworkConnectivityException
import com.websarva.wings.android.zuboradiary.data.network.exception.NetworkOperationException
import com.websarva.wings.android.zuboradiary.data.network.exception.InvalidNetworkRequestParameterException
import com.websarva.wings.android.zuboradiary.data.network.exception.ResponseParsingException
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import com.websarva.wings.android.zuboradiary.di.DispatchersIO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.net.ssl.SSLException

/**
 * 天気情報APIへのデータアクセスを行うデータソースクラス。
 *
 * このクラスは、[WeatherApiService] を使用してOpen-Meteo APIから天気情報を取得する。
 * APIへのアクセス失敗等で発生する特定の例外を [NetworkOperationException] のサブクラスにラップする。
 *
 * @property weatherApiService Retrofitサービスインターフェースのインスタンス。
 * @property dispatcher 天気情報の取得を実行するスレッドプール。
 */
internal class WeatherApiDataSource @Inject constructor(
    private val weatherApiService: WeatherApiService,
    @param:DispatchersIO private val dispatcher: CoroutineDispatcher
) {

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
     * @param date 天気情報を取得する日付。(現在の日付から[MAX_PAST_DAYS]で定義された最大過去日数までの日付の範囲。)
     * @param latitude 天気情報を取得する地点の緯度 (-90.0 から 90.0 の範囲)。
     * @param longitude 天気情報を取得する地点の経度 (-180.0 から 180.0 の範囲)。
     * @return 取得した天気情報データ。
     * @throws InvalidNetworkRequestParameterException 引数が許容範囲外の場合。
     * @throws NetworkConnectivityException ネットワーク接続に問題がある場合。
     * @throws NetworkOperationException ネットワーク操作中に問題が発生した場合 (タイムアウト、SSLエラー、一般的なI/Oエラーなど)。
     * @throws HttpException HTTPレスポンスがエラーを示している場合。
     * @throws ResponseParsingException レスポンスボディがnullまたはパース不可能な場合。
     */
    suspend fun fetchWeatherInfo(
        date: LocalDate,
        latitude: Double,
        longitude: Double
    ): WeatherApiData {
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
     * @throws InvalidNetworkRequestParameterException 引数が不正な範囲の場合。
     * @throws NetworkConnectivityException ネットワーク接続に問題がある場合。
     * @throws NetworkOperationException ネットワーク操作中に問題が発生した場合 (タイムアウト、SSLエラー、一般的なI/Oエラーなど)。
     * @throws HttpException HTTPレスポンスがエラーを示している場合。
     * @throws ResponseParsingException レスポンスボディがnullまたはパース不可能な場合。
     */
    private suspend fun fetchTodayWeatherInfo(latitude: Double, longitude: Double): WeatherApiData {
        requireValidLocation(latitude, longitude)

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
     * @throws InvalidNetworkRequestParameterException 引数が不正な範囲の場合。
     * @throws NetworkConnectivityException ネットワーク接続に問題がある場合。
     * @throws NetworkOperationException ネットワーク操作中に問題が発生した場合 (タイムアウト、SSLエラー、一般的なI/Oエラーなど)。
     * @throws HttpException HTTPレスポンスがエラーを示している場合。
     * @throws ResponseParsingException レスポンスボディがnullまたはパース不可能な場合。
     */
    private suspend fun fetchPastDayWeatherInfo(
        latitude: Double,
        longitude: Double,
        @IntRange(from = MIN_PAST_DAYS.toLong(), to = MAX_PAST_DAYS.toLong())
        numPastDays: Int
    ): WeatherApiData {
        requireValidLocation(latitude, longitude)
        requireValidPastDays(numPastDays)

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
     * @param response Retrofitからのレスポンスオブジェクト。
     * @return 変換された[WeatherApiData]。
     * @throws HttpException HTTPレスポンスがエラーを示している場合。
     * @throws ResponseParsingException レスポンスボディがnullまたはパース不可能な場合。
     */
    private fun toWeatherApiData(response: Response<WeatherApiData>): WeatherApiData {
        Log.d(logTag, "code = " + response.code())
        Log.d(logTag, "message = :" + response.message())

        return if (response.isSuccessful) {
            Log.d(logTag, "body = " + response.body())
            val result = response.body() ?: throw ResponseParsingException()
            result
        } else {
            // HTTPエラー (4xx, 5xx)
            val errorBodyString = response.errorBody()?.string() ?: "null"
            Log.d(logTag, "errorBody = $errorBodyString")
            throw HttpException(
                response.code(),
                response.message(),
                errorBodyString
            )
        }
    }

    /**
     * リトライ機能付きでWeb API操作を実行し、一般的なネットワーク関連の例外をラップする。
     *
     * 以下の例外が発生した場合、[NetworkConnectivityException] や [NetworkOperationException] にラップして再スローします：
     * - [SocketTimeoutException]: タイムアウト (読み取りタイムアウト)
     * - [UnknownHostException]: DNS解決失敗など (インターネット接続なし、ホスト名間違い)
     * - [ConnectException]: サーバー接続失敗など (サーバーダウン、ポートが開いていない)
     * - [SSLException]: SSL/TLSハンドシェイクエラー
     * - [IOException]: その他の予期せぬ接続切断など
     *
     * @param R 操作の結果の型。
     * @param retries 最大リトライ回数 (デフォルト: 3回)。
     * @param initialDelayMillis 初回リトライまでの待機時間 (デフォルト: 1000ms)。
     * @param factor バックオフ係数。2.0なら待機時間が倍々になる (1s, 2s, 4s...)。
     * @param operation 実行するsuspend関数形式のWeb API操作。
     * @return Web API操作の結果。
     * @throws NetworkConnectivityException ネットワーク接続に問題がある場合。
     * @throws NetworkOperationException ネットワーク操作中に問題が発生した場合 (タイムアウト、SSLエラー、一般的なI/Oエラーなど)。
     * @throws IllegalStateException 全てのリトライが失敗し、かつ例外ハンドリングも漏れた場合（理論上到達しない）。
     */
    private suspend fun <R> executeWebApiOperation(
        retries: Int = 3,
        initialDelayMillis: Long = 1000,
        factor: Double = 2.0,
        operation: suspend () -> R
    ): R {
        var currentDelay = initialDelayMillis

        // 指定回数分ループ (+1は初回実行分)
        repeat(retries + 1) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                val isLastAttempt = attempt == retries
                // リトライすべきでない、または最後の試行の場合は例外を処理して終了
                if (!shouldRetry(e) || isLastAttempt) {
                    handleNetworkException(e)
                }

                Log.d("Retry", "API通信に失敗。${currentDelay}ミリ秒後に再試行。(試行回数: ${attempt + 1}) ")

                delay(currentDelay)

                // 次回の待機時間を計算 (指数バックオフ)
                currentDelay = (currentDelay * factor).toLong()
            }
        }
        // ここに来ることはロジック上ないが、コンパイラを通すためにthrowしておく
        throw IllegalStateException("予期せぬエラー（リトライロジック通過）。")
    }

    /**
     * 発生した例外に基づいて、リトライを実行すべきかどうかを判定する。
     *
     * @param e 発生した例外。
     * @return リトライすべき場合は true、即時エラーとすべき場合は false。
     */
    private fun shouldRetry(e: Exception): Boolean {
        return when (e) {
            is SocketTimeoutException -> true // タイムアウトは一時的な可能性が高いためリトライする
            is UnknownHostException -> false  // オフライン等は即時通知すべきためリトライしない
            is ConnectException -> false      // 接続拒否は回復の見込みが薄いためリトライしない
            is SSLException -> false          // 証明書エラー等は自動回復しないためリトライしない
            is IOException -> true            // その他の不明なIOエラーは念のためリトライする
            else -> false                     // 通信以外のエラー（パースエラー等）はリトライしない
        }
    }

    /**
     * ネットワーク関連の例外を適切なカスタム例外に変換してスローする。
     *
     * 発生した例外 [e] を [executeWebApiOperation] の仕様に基づき、
     * [NetworkConnectivityException] または [NetworkOperationException] に変換します。
     *
     * @param e 発生した例外。
     * @return この関数は常に例外を投げるため、値を返しません。
     * @throws NetworkConnectivityException 接続確立に関連するエラーの場合。
     * @throws NetworkOperationException 通信中の操作エラーの場合。
     */
    private fun handleNetworkException(e: Exception): Nothing {
        throw when (e) {
            is SocketTimeoutException -> NetworkOperationException("読み取りタイムアウトエラー", e)
            is UnknownHostException -> NetworkConnectivityException(e)
            is ConnectException -> NetworkConnectivityException(e)
            is SSLException -> NetworkOperationException("SSL/TLS ハンドシェイクエラー", e)
            is IOException -> NetworkOperationException("ネットワークエラー", e)
            else -> e
        }
    }

    /**
     * 指定された緯度と経度が、地理座標として有効な範囲内にあることを要求する。
     *
     * 緯度は -90.0 から 90.0 の範囲、経度は -180.0 から 180.0 の範囲であること。
     *
     * @param latitude 検証する緯度。-90.0 から 90.0 の範囲であること。
     * @param longitude 検証する経度。-180.0 から 180.0 の範囲であること。
     * @throws InvalidNetworkRequestParameterException 緯度または経度が有効な範囲外の場合。
     */
    private fun requireValidLocation(latitude: Double, longitude: Double) {
        try {
            require(latitude >= -90.0 && latitude <= 90.0) { "緯度 `$latitude` が不正値" }
            require(longitude >= -180.0 && longitude <= 180.0) { "経度 `$longitude` が不正値" }
        } catch (e: IllegalArgumentException) {
            throw InvalidNetworkRequestParameterException(e)
        }
    }

    /**
     * 指定された過去日数が、APIからの天気情報取得可能な範囲内にあることを要求する。
     *
     * 日数が [MIN_PAST_DAYS] から [MAX_PAST_DAYS] の範囲外であること。
     *
     * @param numPastDays 検証する過去日数。
     * @throws InvalidNetworkRequestParameterException 過去日数が有効な範囲外の場合。
     */
    private fun requireValidPastDays(
        @IntRange(from = MIN_PAST_DAYS.toLong(), to = MAX_PAST_DAYS.toLong()) numPastDays: Int
    ) {
        try {
            require(
                numPastDays in MIN_PAST_DAYS..MAX_PAST_DAYS
            ) { "過去日数 `$numPastDays` が不正値" }
        } catch (e: IllegalArgumentException) {
            throw InvalidNetworkRequestParameterException( e)
        }
    }
}
