package com.websarva.wings.android.zuboradiary.data.network;

import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherApiRepository {
    private WeatherApiService service;
    private final String QUERY_DAIRY_PARAMETER = "weather_code";
    private final String QUERY_TIME_ZONE_PARAMETER = "Asia/Tokyo";
    private final String QUERY_FORECAST_DAYS_PARAMETER_ONLY_TODAY = "1";
    private final String QUERY_FORECAST_DAYS_PARAMETER_NONE = "0";
    private final String QUERY_PAST_DAYS_PARAMETER_NONE = "0";
    public WeatherApiRepository() {
        service = WeatherApiRetrofit.getInstance().getService();
    }

    public Call<WeatherApiResponse> getTodayWeather(
            double latitude, double longitude) throws IllegalArgumentException {
        if (latitude < 0 || longitude < 0) {
            throw new IllegalArgumentException();
        }
        return service.getWeather(
                String.valueOf(latitude),
                String.valueOf(longitude),
                QUERY_DAIRY_PARAMETER,
                QUERY_TIME_ZONE_PARAMETER,
                QUERY_PAST_DAYS_PARAMETER_NONE,
                QUERY_FORECAST_DAYS_PARAMETER_ONLY_TODAY);
    }

    public Call<WeatherApiResponse> getPastDayWeather(
            double latitude, double longitude, int numPastDays) throws IllegalArgumentException {
        if (latitude < 0 || longitude < 0 || numPastDays > 92 || numPastDays < 1) {
            throw new IllegalArgumentException();
        }
        Log.d("20240717", "getPastDayWeather");
        return service.getWeather(
                String.valueOf(latitude),
                String.valueOf(longitude),
                QUERY_DAIRY_PARAMETER,
                QUERY_TIME_ZONE_PARAMETER,
                String.valueOf(numPastDays),
                QUERY_FORECAST_DAYS_PARAMETER_NONE);
    }
}
