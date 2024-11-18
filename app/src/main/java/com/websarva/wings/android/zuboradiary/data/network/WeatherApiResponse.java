package com.websarva.wings.android.zuboradiary.data.network;

import android.util.Log;

import androidx.annotation.NonNull;

import com.squareup.moshi.Json;
import com.websarva.wings.android.zuboradiary.data.diary.Weather;

import java.util.Arrays;
import java.util.Objects;

public class WeatherApiResponse {

    // MEMO:フィールド変数はRetrofit2(Moshi)にて代入。
    private float latitude;
    private float longitude;
    private WeatherApiResponseDairy daily;

    private WeatherApiResponse() {
        // HACK:Retrofit2(Moshi)を使用して本クラスをインスタンス化する時、引数ありのコンストラクタは処理されない。
        //      引数なしコンストラクタ処理時は、フィールド変数の値は未格納。
        //      原因が明らかになるまで、フィールド変数参照時はNullチェック等を行う。
    }

    @NonNull
    public Weather toWeatherInfo() {
        new GeoCoordinates(latitude, longitude); // GeoCoordinatesのコンストラクタを使用してlatitude、longitudeの値チェック
        Log.d("WeatherApi", "latitude:" + latitude);
        Log.d("WeatherApi", "longitude:" + longitude);
        Objects.requireNonNull(daily);
        for (String s: daily.getTimes()) Log.d("WeatherApi", "time:" + s);
        for (int i: daily.getWeatherCodes()) Log.d("WeatherApi", "weatherCode:" + i);

        int[] weatherCodes = daily.getWeatherCodes();
        int weatherCode = weatherCodes[0];
        return convertWeathers(weatherCode);
    }

    // "apiWeatherCode"は下記ページの"WMO 気象解釈コード"
    // https://open-meteo.com/en/docs
    @NonNull
    private Weather convertWeathers(int apiWeatherCode) {
        Log.d("WeatherApi", String.valueOf(apiWeatherCode));
        switch (apiWeatherCode) {
            case 0:
            case 1: case 2:
                return Weather.SUNNY;
            case 3:
            case 45: case 48:
                return Weather.CLOUDY;
            case 51: case 53: case 55:
            case 56: case 57:
            case 61: case 63: case 65:
            case 66: case 67:
            case 80: case 81: case 82:
            case 95:
            case 96: case 99:
                return Weather.RAINY;
            case 71: case 73: case 75:
            case 77:
            case 85: case 86:
                return Weather.SNOWY;
            default:
                return Weather.UNKNOWN;
        }
    }

    private static class WeatherApiResponseDairy {

        // MEMO:フィールド変数はRetrofit2(Moshi)にて代入。
        @Json(name = "time")
        private String[] times;
        @Json(name = "weather_code")
        private int[] weatherCodes;

        private WeatherApiResponseDairy() {
            // HACK:Retrofit2(Moshi)を使用して本クラスをインスタンス化する時、引数ありのコンストラクタは処理されない。
            //      引数なしコンストラクタ処理時は、フィールド変数の値は未格納。
            //      原因が明らかになるまで、フィールド変数参照時はNullチェック等を行う。
        }

        private String[] getTimes() {
            Objects.requireNonNull(times);
            Arrays.stream(times).forEach(Objects::requireNonNull);

            return times;
        }

        private int[] getWeatherCodes() {
            Objects.requireNonNull(weatherCodes);

            return weatherCodes;
        }
    }
}
