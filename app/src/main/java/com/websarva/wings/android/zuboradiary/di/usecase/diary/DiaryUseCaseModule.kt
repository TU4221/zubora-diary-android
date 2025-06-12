package com.websarva.wings.android.zuboradiary.di.usecase.diary

import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.LocationRepository
import com.websarva.wings.android.zuboradiary.data.repository.WeatherApiRepository
import com.websarva.wings.android.zuboradiary.data.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.CanLoadWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.ShouldRequestDiaryUpdateConfirmationUseCase
import com.websarva.wings.android.zuboradiary.data.usecase.diary.LoadWeatherInfoUseCase
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
internal object DiaryUseCaseModule {

    @Singleton
    @Provides
    fun provideDoesDiaryExistUseCase(
        diaryRepository: DiaryRepository
    ): DoesDiaryExistUseCase {
        return DoesDiaryExistUseCase(diaryRepository)
    }

    @Singleton
    @Provides
    fun provideCanLoadWeatherInfoUseCase(
        weatherApiRepository: WeatherApiRepository
    ): CanLoadWeatherInfoUseCase {
        return CanLoadWeatherInfoUseCase(weatherApiRepository)
    }

    @Singleton
    @Provides
    fun provideLoadWeatherInfoUseCase(
        weatherApiRepository: WeatherApiRepository,
        locationRepository: LocationRepository,
        canLoadWeatherInfoUseCase: CanLoadWeatherInfoUseCase
    ): LoadWeatherInfoUseCase {
        return LoadWeatherInfoUseCase(
            weatherApiRepository,
            locationRepository,
            canLoadWeatherInfoUseCase
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
