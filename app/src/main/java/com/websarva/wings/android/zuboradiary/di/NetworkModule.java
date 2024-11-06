package com.websarva.wings.android.zuboradiary.di;

import androidx.annotation.NonNull;

import com.squareup.moshi.Moshi;
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiService;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

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
    @NonNull
    public static KotlinJsonAdapterFactory provideKotlinJsonAdapterFactory() {
        return new KotlinJsonAdapterFactory();
    }

    @Singleton
    @Provides
    @NonNull
    public static Moshi provideMoshi(KotlinJsonAdapterFactory kotlinJsonAdapterFactory) {
        Objects.requireNonNull(kotlinJsonAdapterFactory);

        Moshi moshi = new Moshi.Builder().add(kotlinJsonAdapterFactory).build();
        return Objects.requireNonNull(moshi);
    }

    @Singleton
    @Provides
    @NonNull
    public static MoshiConverterFactory provideMoshiConverterFactory(Moshi moshi) {
        Objects.requireNonNull(moshi);

        MoshiConverterFactory factory = MoshiConverterFactory.create(moshi);
        return Objects.requireNonNull(factory);
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    private @interface WeatherApiRetrofit {}

    @WeatherApiRetrofit
    @Singleton
    @Provides
    @NonNull
    public static Retrofit provideWeatherApiRetrofit(MoshiConverterFactory moshiConverterFactory) {
        Objects.requireNonNull(moshiConverterFactory);

        Retrofit retrofit =
                new Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/v1/")
                .addConverterFactory(moshiConverterFactory)
                .build();
        return Objects.requireNonNull(retrofit);
    }

    @Singleton
    @Provides
    @NonNull
    public static WeatherApiService provideWeatherApiService(@WeatherApiRetrofit Retrofit retrofit) {
        Objects.requireNonNull(retrofit);

        WeatherApiService weatherApiService = retrofit.create(WeatherApiService.class);
        return Objects.requireNonNull(weatherApiService);
    }
}
