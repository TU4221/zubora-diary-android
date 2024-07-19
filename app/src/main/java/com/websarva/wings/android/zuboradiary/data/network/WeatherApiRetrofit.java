package com.websarva.wings.android.zuboradiary.data.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// TODO:公式ではHiltを推奨しているため修正の必要あり。
//      https://developer.android.com/training/dependency-injection?hl=ja#java
public class WeatherApiRetrofit {
    private static WeatherApiRetrofit instance;
    private WeatherApiService service;
    private static final String BASE_URL = "https://api.open-meteo.com/v1/";

    private WeatherApiRetrofit(){
        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
        service = retrofit.create(WeatherApiService.class);
    }

    public static WeatherApiRetrofit getInstance() {
        if (instance == null) {
            instance = new WeatherApiRetrofit();
        }
        return instance;
    }

    public WeatherApiService getService() {
        return service;
    }
}
