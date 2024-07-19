package com.websarva.wings.android.zuboradiary.data.diary;

import android.content.Context;

import com.websarva.wings.android.zuboradiary.R;

public enum Weathers {
    UNKNOWN(0, R.string.enum_weather_unknown),
    SUNNY(1, R.string.enum_weather_sunny),
    CLOUDY(2, R.string.enum_weather_cloudy),
    RAINY(3, R.string.enum_weather_rainy),
    SNOWY(4, R.string.enum_weather_snowy);
    private int weatherNumber;
    private int weatherNameResId;

    private Weathers(int weatherNumber, int weatherNameResId) {
        this.weatherNumber = weatherNumber;
        this.weatherNameResId = weatherNameResId;
    }

    public String toString(Context context) {
        return context.getString(this.weatherNameResId);
    }

    public int toWeatherNumber() {
        return this.weatherNumber;
    }
}
