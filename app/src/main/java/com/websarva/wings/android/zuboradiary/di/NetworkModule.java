package com.websarva.wings.android.zuboradiary.di;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiRepository;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiService;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Singleton
    @Provides
    public static KotlinJsonAdapterFactory provideKotlinJsonAdapterFactory() {
        return new KotlinJsonAdapterFactory();
    }

    @Singleton
    @Provides
    public static Moshi provideMoshi(KotlinJsonAdapterFactory kotlinJsonAdapterFactory) {
        return new Moshi.Builder().add(kotlinJsonAdapterFactory).build();
    }

    @Singleton
    @Provides
    public static MoshiConverterFactory provideMoshiConverterFactory(Moshi moshi) {
        return MoshiConverterFactory.create(moshi);
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    private @interface WeatherApiRetrofit {}

    @WeatherApiRetrofit
    @Singleton
    @Provides
    public static Retrofit provideWeatherApiRetrofit(MoshiConverterFactory moshiConverterFactory) {
        return new Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/v1/")
                .addConverterFactory(moshiConverterFactory)
                .build();
    }

    @Singleton
    @Provides
    public static WeatherApiService provideWeatherApiService(@WeatherApiRetrofit Retrofit retrofit) {
        return retrofit.create(WeatherApiService.class);
    }
}
