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
    private final int MIN_PAST_DAYS = 1; //過去天気情報取得可能最小日
    private final int MAX_PAST_DAYS = 92; //過去天気情報取得可能最大日

    @Inject
    public WeatherApiRepository(WeatherApiService weatherApiService) {
        Objects.requireNonNull(weatherApiService);

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
        Objects.requireNonNull(geoCoordinates);

        return weatherApiService.getWeather(
                String.valueOf(geoCoordinates.getLatitude()),
                String.valueOf(geoCoordinates.getLongitude()),
                QUERY_DAIRY_PARAMETER,
                QUERY_TIME_ZONE_PARAMETER,
                "0" /*today*/,
                "1" /*1日分*/);
    }

    public Call<WeatherApiResponse> fetchPastDayWeatherInfo(
            GeoCoordinates geoCoordinates, @IntRange(from = MIN_PAST_DAYS, to = MAX_PAST_DAYS) int numPastDays) {
        Objects.requireNonNull(geoCoordinates);
        if (numPastDays < MIN_PAST_DAYS || numPastDays > MAX_PAST_DAYS) throw new IllegalArgumentException();

        return weatherApiService.getWeather(
                String.valueOf(geoCoordinates.getLatitude()),
                String.valueOf(geoCoordinates.getLongitude()),
                QUERY_DAIRY_PARAMETER,
                QUERY_TIME_ZONE_PARAMETER,
                String.valueOf(numPastDays),
                "0" /*1日分(過去日から1日分取得する場合"0"を代入)*/);
    }
}
