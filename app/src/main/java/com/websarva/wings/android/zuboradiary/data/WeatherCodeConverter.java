package com.websarva.wings.android.zuboradiary.data;


import com.websarva.wings.android.zuboradiary.data.diary.Weathers;

public class WeatherCodeConverter {
    // "apiWeatherCode"は下記ページの"WMO 気象解釈コード"
    // https://open-meteo.com/en/docs
    public Weathers convertWeathers(int apiWeatherCode) {
        switch (apiWeatherCode) {
            case 0:
            case 1:
                return Weathers.SUNNY;
            case 2: case 3:
            case 45: case 48:
                return Weathers.CLOUDY;
            case 51: case 53: case 55:
            case 56: case 57:
            case 61: case 63: case 65:
            case 66: case 67:
            case 80: case 81: case 82:
            case 95:
            case 96: case 99:
                return Weathers.RAINY;
            case 71: case 73: case 75:
            case 77:
            case 85: case 86:
                return Weathers.SNOWY;
            default:
                return Weathers.UNKNOWN;
        }
    }
}
