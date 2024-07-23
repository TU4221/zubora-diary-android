package com.websarva.wings.android.zuboradiary.data.network;

import com.google.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

// TODO:公式ではHiltを推奨しているため修正の必要あり。
//      https://developer.android.com/training/dependency-injection?hl=ja#java
public class WeatherApiRetrofit {
    private static WeatherApiRetrofit instance;
    private WeatherApiService service;
    private static final String BASE_URL = "https://api.open-meteo.com/v1/";

    private WeatherApiRetrofit(){
        Moshi.Builder builder = new Moshi.Builder();
        Moshi moshi = builder.add(new KotlinJsonAdapterFactory()).build();
        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        //.addConverterFactory(GsonConverterFactory.create())
                        .addConverterFactory(MoshiConverterFactory.create(moshi))
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
