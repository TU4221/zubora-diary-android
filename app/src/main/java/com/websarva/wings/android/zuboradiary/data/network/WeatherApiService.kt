package com.websarva.wings.android.zuboradiary.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
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
