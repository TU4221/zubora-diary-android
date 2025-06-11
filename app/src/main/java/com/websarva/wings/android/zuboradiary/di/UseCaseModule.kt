package com.websarva.wings.android.zuboradiary.di

import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.LocationRepository
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import com.websarva.wings.android.zuboradiary.data.repository.WeatherApiRepository
import com.websarva.wings.android.zuboradiary.data.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.CanFetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.ShouldRequestDiaryUpdateConfirmationUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.FetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.SaveDiaryUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.uri.ReleaseUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.uri.TakeUriPermissionUseCase
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
    fun provideDoesDiaryExistUseCase(
        diaryRepository: DiaryRepository
    ): DoesDiaryExistUseCase {
        return DoesDiaryExistUseCase(diaryRepository)
    }

    @Singleton
    @Provides
    fun provideTakeUriPermissionUseCase(
        uriRepository: UriRepository
    ): TakeUriPermissionUseCase {
        return TakeUriPermissionUseCase(uriRepository)
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
    fun provideCanFetchWeatherInfoUseCase(
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

    @Singleton
    @Provides
    fun provideShouldRequestDiaryUpdateConfirmationUseCase(
        doesDiaryExistUseCase: DoesDiaryExistUseCase
    ): ShouldRequestDiaryUpdateConfirmationUseCase {
        return ShouldRequestDiaryUpdateConfirmationUseCase(
            doesDiaryExistUseCase
        )
    }

    @Singleton
    @Provides
    fun provideSaveDiaryUseCase(
        diaryRepository: DiaryRepository,
        takeUriPermissionUseCase: TakeUriPermissionUseCase,
        releaseUriPermissionUseCase: ReleaseUriPermissionUseCase,
    ): SaveDiaryUseCase {
        return SaveDiaryUseCase(
            diaryRepository,
            takeUriPermissionUseCase,
            releaseUriPermissionUseCase
        )
    }
}
