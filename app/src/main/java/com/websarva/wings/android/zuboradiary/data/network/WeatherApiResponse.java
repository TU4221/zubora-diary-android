package com.websarva.wings.android.zuboradiary.data.network;

public class WeatherApiResponse {
    private float latitude;
    private float longitude;
    private WeatherApiResponseDairy daily;

    public WeatherApiResponse(float latitude, float longitude, WeatherApiResponseDairy daily) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.daily = daily;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public WeatherApiResponseDairy getDaily() {
        return daily;
    }

    public void setDaily(WeatherApiResponseDairy daily) {
        this.daily = daily;
    }
}
