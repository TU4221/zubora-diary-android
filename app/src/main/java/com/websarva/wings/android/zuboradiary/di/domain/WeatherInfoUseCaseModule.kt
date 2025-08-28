package com.websarva.wings.android.zuboradiary.di.domain

import com.websarva.wings.android.zuboradiary.domain.repository.WeatherInfoRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo.CanFetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo.FetchWeatherInfoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object WeatherInfoUseCaseModule {

    @Singleton
    @Provides
    fun provideCanFetchWeatherInfoUseCase(
        weatherInfoRepository: WeatherInfoRepository
    ): CanFetchWeatherInfoUseCase = CanFetchWeatherInfoUseCase(weatherInfoRepository)

    @Singleton
    @Provides
    fun provideFetchWeatherInfoUseCase(
        weatherInfoRepository: WeatherInfoRepository,
        canFetchWeatherInfoUseCase: CanFetchWeatherInfoUseCase
    ): FetchWeatherInfoUseCase =
        FetchWeatherInfoUseCase(weatherInfoRepository, canFetchWeatherInfoUseCase)
}
