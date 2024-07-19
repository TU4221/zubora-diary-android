package com.websarva.wings.android.zuboradiary.data.network;

import com.squareup.moshi.Json;

public class WeatherApiResponseDairy {
    @Json(name = "time")
    private String[] times;
    @Json(name = "weather_code")
    private int[] weatherCodes;

    public WeatherApiResponseDairy(String[] times, int[] weatherCodes) {
        this.times = times;
        this.weatherCodes = weatherCodes;
    }

    public String[] getTimes() {
        return times;
    }

    public void setTimes(String[] times) {
        this.times = times;
    }

    public int[] getWeatherCodes() {
        return weatherCodes;
    }

    public void setWeatherCode(int[] weatherCodes) {
        this.weatherCodes = weatherCodes;
    }
}
