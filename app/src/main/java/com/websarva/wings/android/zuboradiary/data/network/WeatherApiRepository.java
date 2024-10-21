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

    public Call<WeatherApiResponse> getTodayWeather(GeoCoordinates geoCoordinates) {
        return weatherApiService.getWeather(
                String.valueOf(geoCoordinates.getLatitude()),
                String.valueOf(geoCoordinates.getLongitude()),
                QUERY_DAIRY_PARAMETER,
                QUERY_TIME_ZONE_PARAMETER,
                QUERY_PAST_DAYS_PARAMETER_NONE,
                QUERY_FORECAST_DAYS_PARAMETER_ONLY_TODAY);
    }

    public Call<WeatherApiResponse> getPastDayWeather(
            GeoCoordinates geoCoordinates, @IntRange(from = 1, to = 92) int numPastDays) {
        return weatherApiService.getWeather(
                String.valueOf(geoCoordinates.getLatitude()),
                String.valueOf(geoCoordinates.getLongitude()),
                QUERY_DAIRY_PARAMETER,
                QUERY_TIME_ZONE_PARAMETER,
                String.valueOf(numPastDays),
                QUERY_FORECAST_DAYS_PARAMETER_NONE);
    }
}
