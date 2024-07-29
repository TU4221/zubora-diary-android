package com.websarva.wings.android.zuboradiary.data.network;

import android.util.Log;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;

import javax.inject.Inject;

import retrofit2.Call;

public class WeatherApiRepository {
    private WeatherApiService weatherApiService;
    private final String QUERY_DAIRY_PARAMETER = "weather_code";
    private final String QUERY_TIME_ZONE_PARAMETER = "Asia/Tokyo";
    private final String QUERY_FORECAST_DAYS_PARAMETER_ONLY_TODAY = "1";
    private final String QUERY_FORECAST_DAYS_PARAMETER_NONE = "0";
    private final String QUERY_PAST_DAYS_PARAMETER_NONE = "0";

    @Inject
    public WeatherApiRepository(WeatherApiService weatherApiService) {
        this.weatherApiService = weatherApiService;
    }

    public Call<WeatherApiResponse> getTodayWeather(
            @FloatRange(from = -90.0, to = 90.0) double latitude,
            @FloatRange(from = -180.0, to = 180.0) double longitude) {
        return weatherApiService.getWeather(
                String.valueOf(latitude),
                String.valueOf(longitude),
                QUERY_DAIRY_PARAMETER,
                QUERY_TIME_ZONE_PARAMETER,
                QUERY_PAST_DAYS_PARAMETER_NONE,
                QUERY_FORECAST_DAYS_PARAMETER_ONLY_TODAY);
    }

    public Call<WeatherApiResponse> getPastDayWeather(
            @FloatRange(from = -90.0, to = 90.0) double latitude,
            @FloatRange(from = -180.0, to = 180.0) double longitude,
            @IntRange(from = 1, to = 92) int numPastDays) {
        Log.d("20240717", "getPastDayWeather");
        return weatherApiService.getWeather(
                String.valueOf(latitude),
                String.valueOf(longitude),
                QUERY_DAIRY_PARAMETER,
                QUERY_TIME_ZONE_PARAMETER,
                String.valueOf(numPastDays),
                QUERY_FORECAST_DAYS_PARAMETER_NONE);
    }
}
