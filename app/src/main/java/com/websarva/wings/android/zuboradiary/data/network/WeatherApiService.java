package com.websarva.wings.android.zuboradiary.data.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {
    @GET("forecast")
    Call<WeatherApiResponse> getWeather(
            @Query("latitude") String latitude,
            @Query("longitude") String longitude,
            @Query("daily") String daily,
            @Query("timezone") String timezone,
            @Query("past_days") String past_days,
            @Query("forecast_days") String forecast_days);
}
