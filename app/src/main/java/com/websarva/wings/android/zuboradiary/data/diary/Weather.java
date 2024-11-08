package com.websarva.wings.android.zuboradiary.data.diary;

import android.content.Context;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.R;

import java.util.Arrays;
import java.util.Objects;

public enum Weather {

    UNKNOWN(0, R.string.enum_weather_unknown),
    SUNNY(1, R.string.enum_weather_sunny),
    CLOUDY(2, R.string.enum_weather_cloudy),
    RAINY(3, R.string.enum_weather_rainy),
    SNOWY(4, R.string.enum_weather_snowy);

    private final int number;
    private final int stringResId;

    Weather(int number, int stringResId) {
        this.number = number;
        this.stringResId = stringResId;
    }

    @NonNull
    public static Weather of(int number) {
        return Arrays.stream(Weather.values()).filter(x -> x.toNumber() == number).findFirst().get();
    }

    @NonNull
    public static Weather of(Context context, String strWeather) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(strWeather);

        return Arrays.stream(Weather.values())
                .filter(x -> x.toString(context).equals(strWeather)).findFirst().get();
    }

    @NonNull
    public String toString(Context context) {
        Objects.requireNonNull(context);

        String string = context.getString(stringResId);
        return Objects.requireNonNull(string);
    }

    public int toNumber() {
        return number;
    }
}
