package com.websarva.wings.android.zuboradiary.di.usecase.diary

import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.LocationRepository
import com.websarva.wings.android.zuboradiary.data.repository.WeatherApiRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CanLoadWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryUpdateConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.SaveDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldLoadWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryLoadingConfirmationUseCase
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
    fun provideShouldRequestDiaryLoadingConfirmationUseCase(
        doesDiaryExistUseCase: DoesDiaryExistUseCase
    ): ShouldRequestDiaryLoadingConfirmationUseCase {
        return ShouldRequestDiaryLoadingConfirmationUseCase(
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
        shouldLoadWeatherInfoUseCase: ShouldLoadWeatherInfoUseCase
    ): ShouldRequestWeatherInfoConfirmationUseCase {
        return ShouldRequestWeatherInfoConfirmationUseCase(shouldLoadWeatherInfoUseCase)
    }

    @Singleton
    @Provides
    fun provideLoadDiaryUseCase(
        diaryRepository: DiaryRepository
    ): LoadDiaryUseCase {
        return LoadDiaryUseCase(diaryRepository)
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
    fun provideShouldLoadWeatherInfoUseCase(): ShouldLoadWeatherInfoUseCase {
        return ShouldLoadWeatherInfoUseCase()
    }

    @Singleton
    @Provides
    fun provideShouldRequestExitWithoutDiarySavingConfirmationUseCase() =
        ShouldRequestExitWithoutDiarySavingConfirmationUseCase()
}
