package com.websarva.wings.android.zuboradiary.data.diary;

import android.content.Context;

import androidx.annotation.Nullable;

public class WeatherConverter {
    public WeatherConverter() {}
    public Weathers toWeather(@Nullable Integer weatherCode) {
        if (weatherCode == null) {
            return Weathers.UNKNOWN;
        }
        for (Weathers weather: Weathers.values()) {
            if (weatherCode == weather.toWeatherNumber()) {
                return weather;
            }
        }
        return Weathers.UNKNOWN;
    }
    public Weathers toWeather(Context context, String strWeather) {
        for (Weathers weather: Weathers.values()) {
            if (strWeather.equals(weather.toString(context))) {
                return weather;
            }
        }
        return Weathers.UNKNOWN;
    }
}
