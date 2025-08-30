package com.websarva.wings.android.zuboradiary.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo APIへのアクセスを提供するRetrofitインターフェース。
 *
 * このインターフェースは、天気予報データを取得するためのAPIエンドポイントを定義する。
 * 詳細は [Open-Meteo API Documentation](https://open-meteo.com/en/docs) を参照。
 */
internal interface WeatherApiService {

    /**
     * 指定された条件に基づいて天気予報データを取得する。
     * 詳細は [Open-Meteo API Documentation](https://open-meteo.com/en/docs) を参照。
     *
     * @param latitude 予報を取得する地点の緯度。
     * @param longitude 予報を取得する地点の経度。
     * @param daily 取得するデータの種類 (例: "weather_code")。
     * @param timezone 使用するタイムゾーン (例: "Asia/Tokyo")。
     * @param pastDays 過去何日からのデータを取得するか (例: "今日からなら"0")。
     * @param forecastDays 何日分のデータを取得するか。過去日から取得する場合"0"から数える。
     * @return APIからのレスポンス。
     */
    @GET("forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
        @Query("daily") daily: String,
        @Query("timezone") timezone: String,
        @Query("past_days") pastDays: String,
        @Query("forecast_days") forecastDays: String
    ): Response<WeatherApiData>
}
