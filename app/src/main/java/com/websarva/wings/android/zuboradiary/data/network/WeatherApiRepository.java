package com.websarva.wings.android.zuboradiary.data.network;

import android.util.Log;

import androidx.annotation.IntRange;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import javax.inject.Inject;

import retrofit2.Call;

public class WeatherApiRepository {
    private final WeatherApiService weatherApiService;
    private final String QUERY_DAIRY_PARAMETER = "weather_code";
    private final String QUERY_TIME_ZONE_PARAMETER = "Asia/Tokyo";
    private final String QUERY_FORECAST_DAYS_PARAMETER_ONLY_TODAY = "1";
    private final String QUERY_FORECAST_DAYS_PARAMETER_NONE = "0";
    private final String QUERY_PAST_DAYS_PARAMETER_NONE = "0";
    private final int MAX_PAST_DAYS = 92; //過去天気情報取得可能日

    @Inject
    public WeatherApiRepository(WeatherApiService weatherApiService) {
        this.weatherApiService = weatherApiService;
    }

    public boolean canFetchWeatherInfo(LocalDate date) {
        Objects.requireNonNull(date);

        LocalDate currentDate = LocalDate.now();
        Log.d("fetchWeatherInfo", "isAfter:" + date.isAfter(currentDate));
        if (date.isAfter(currentDate)) return false;
        long betweenDays = ChronoUnit.DAYS.between(date, currentDate);
        Log.d("fetchWeatherInfo", "betweenDays:" + betweenDays);

        return betweenDays <= MAX_PAST_DAYS;
    }

    public Call<WeatherApiResponse> fetchTodayWeatherInfo(GeoCoordinates geoCoordinates) {
        return weatherApiService.getWeather(
                String.valueOf(geoCoordinates.getLatitude()),
                String.valueOf(geoCoordinates.getLongitude()),
                QUERY_DAIRY_PARAMETER,
                QUERY_TIME_ZONE_PARAMETER,
                QUERY_PAST_DAYS_PARAMETER_NONE,
                QUERY_FORECAST_DAYS_PARAMETER_ONLY_TODAY);
    }

    public Call<WeatherApiResponse> fetchPastDayWeatherInfo(
            GeoCoordinates geoCoordinates, @IntRange(from = 1, to = MAX_PAST_DAYS) int numPastDays) {
        return weatherApiService.getWeather(
                String.valueOf(geoCoordinates.getLatitude()),
                String.valueOf(geoCoordinates.getLongitude()),
                QUERY_DAIRY_PARAMETER,
                QUERY_TIME_ZONE_PARAMETER,
                String.valueOf(numPastDays),
                QUERY_FORECAST_DAYS_PARAMETER_NONE);
    }
}
