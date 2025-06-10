package com.websarva.wings.android.zuboradiary.di

import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.LocationRepository
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import com.websarva.wings.android.zuboradiary.data.repository.WeatherApiRepository
import com.websarva.wings.android.zuboradiary.data.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.CanFetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.FetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.ReleaseUriPermissionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object UseCaseModule {

    @Singleton
    @Provides
    fun provideCheckDiaryExistsUseCase(
        diaryRepository: DiaryRepository
    ): DoesDiaryExistUseCase {
        return DoesDiaryExistUseCase(diaryRepository)
    }

    @Singleton
    @Provides
    fun provideReleaseUriPermissionUseCase(
        uriRepository: UriRepository,
        diaryRepository: DiaryRepository
    ): ReleaseUriPermissionUseCase {
        return ReleaseUriPermissionUseCase(uriRepository, diaryRepository)
    }

    @Singleton
    @Provides
    fun provideCheckWeatherInfoFetchabilityUseCase(
        weatherApiRepository: WeatherApiRepository
    ): CanFetchWeatherInfoUseCase {
        return CanFetchWeatherInfoUseCase(weatherApiRepository)
    }

    @Singleton
    @Provides
    fun provideFetchWeatherInfoUseCase(
        weatherApiRepository: WeatherApiRepository,
        locationRepository: LocationRepository,
        canFetchWeatherInfoUseCase: CanFetchWeatherInfoUseCase
    ): FetchWeatherInfoUseCase {
        return FetchWeatherInfoUseCase(
            weatherApiRepository,
            locationRepository,
            canFetchWeatherInfoUseCase
        )
    }
}
