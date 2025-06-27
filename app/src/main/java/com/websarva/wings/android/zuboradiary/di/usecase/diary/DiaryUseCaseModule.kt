package com.websarva.wings.android.zuboradiary.di.usecase.diary

import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.LocationRepository
import com.websarva.wings.android.zuboradiary.data.repository.WeatherInfoRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CanFetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.FetchDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryUpdateConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.FetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.SaveDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldFetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryFetchConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestExitWithoutDiarySavingConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestWeatherInfoConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleaseUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.TakeUriPermissionUseCase
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
    fun provideCanFetchWeatherInfoUseCase(
        weatherInfoRepository: WeatherInfoRepository
    ): CanFetchWeatherInfoUseCase {
        return CanFetchWeatherInfoUseCase(weatherInfoRepository)
    }

    @Singleton
    @Provides
    fun provideFetchWeatherInfoUseCase(
        weatherInfoRepository: WeatherInfoRepository,
        locationRepository: LocationRepository,
        canFetchWeatherInfoUseCase: CanFetchWeatherInfoUseCase
    ): FetchWeatherInfoUseCase {
        return FetchWeatherInfoUseCase(
            weatherInfoRepository,
            locationRepository,
            canFetchWeatherInfoUseCase
        )
    }

    @Singleton
    @Provides
    fun provideShouldRequestDiaryFetchConfirmationUseCase(
        doesDiaryExistUseCase: DoesDiaryExistUseCase
    ): ShouldRequestDiaryFetchConfirmationUseCase {
        return ShouldRequestDiaryFetchConfirmationUseCase(
            doesDiaryExistUseCase
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
    fun provideShouldRequestWeatherInfoConfirmationUseCase(
        shouldFetchWeatherInfoUseCase: ShouldFetchWeatherInfoUseCase
    ): ShouldRequestWeatherInfoConfirmationUseCase {
        return ShouldRequestWeatherInfoConfirmationUseCase(shouldFetchWeatherInfoUseCase)
    }

    @Singleton
    @Provides
    fun provideFetchDiaryUseCase(
        diaryRepository: DiaryRepository
    ): FetchDiaryUseCase {
        return FetchDiaryUseCase(diaryRepository)
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

    @Singleton
    @Provides
    fun provideDeleteDiaryUseCase(
        diaryRepository: DiaryRepository,
        releaseUriPermissionUseCase: ReleaseUriPermissionUseCase,
    ): DeleteDiaryUseCase {
        return DeleteDiaryUseCase(
            diaryRepository,
            releaseUriPermissionUseCase
        )
    }

    @Singleton
    @Provides
    fun provideShouldFetchWeatherInfoUseCase(): ShouldFetchWeatherInfoUseCase {
        return ShouldFetchWeatherInfoUseCase()
    }

    @Singleton
    @Provides
    fun provideShouldRequestExitWithoutDiarySavingConfirmationUseCase() =
        ShouldRequestExitWithoutDiarySavingConfirmationUseCase()
}
