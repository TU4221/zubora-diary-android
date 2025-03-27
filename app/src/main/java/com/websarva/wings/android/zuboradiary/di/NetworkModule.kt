package com.websarva.wings.android.zuboradiary.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Singleton
    @Provides
    fun provideKotlinJsonAdapterFactory(): KotlinJsonAdapterFactory {
        return KotlinJsonAdapterFactory()
    }

    @Singleton
    @Provides
    fun provideMoshi(kotlinJsonAdapterFactory: KotlinJsonAdapterFactory): Moshi {
        return Moshi.Builder().add(kotlinJsonAdapterFactory).build()
    }

    @Singleton
    @Provides
    fun provideMoshiConverterFactory(moshi: Moshi): MoshiConverterFactory {
        return MoshiConverterFactory.create(moshi)
    }

    @Singleton
    @Provides
    fun provideWeatherApiRetrofit(moshiConverterFactory: MoshiConverterFactory): Retrofit {
        return Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/v1/")
                .addConverterFactory(moshiConverterFactory)
                .build()
    }

    @Singleton
    @Provides
    fun provideWeatherApiService(retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }
}
