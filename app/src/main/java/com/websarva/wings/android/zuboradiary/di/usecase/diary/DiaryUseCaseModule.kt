package com.websarva.wings.android.zuboradiary.di.usecase.diary

import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.WeatherInfoRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CanFetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CheckUnloadedDiariesExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CheckUnloadedWordSearchResultDiariesExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CountDiariesUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CountWordSearchResultDiariesUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryItemTitleSelectionHistoryItemUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryItemTitleSelectionHistoryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadNewestDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadOldestDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryUpdateConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.FetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadWordSearchResultDiaryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.SaveDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldFetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryLoadConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestExitWithoutDiarySaveConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestWeatherInfoConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleasePersistableUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.TakePersistableUriPermissionUseCase
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
        canFetchWeatherInfoUseCase: CanFetchWeatherInfoUseCase
    ): FetchWeatherInfoUseCase {
        return FetchWeatherInfoUseCase(
            weatherInfoRepository,
            canFetchWeatherInfoUseCase
        )
    }

    @Singleton
    @Provides
    fun provideShouldRequestDiaryLoadConfirmationUseCase(
        doesDiaryExistUseCase: DoesDiaryExistUseCase
    ): ShouldRequestDiaryLoadConfirmationUseCase {
        return ShouldRequestDiaryLoadConfirmationUseCase(
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
    fun provideLoadDiaryUseCase(
        diaryRepository: DiaryRepository
    ): LoadDiaryUseCase {
        return LoadDiaryUseCase(diaryRepository)
    }

    @Singleton
    @Provides
    fun provideSaveDiaryUseCase(
        diaryRepository: DiaryRepository,
        takePersistableUriPermissionUseCase: TakePersistableUriPermissionUseCase,
        releasePersistableUriPermissionUseCase: ReleasePersistableUriPermissionUseCase,
    ): SaveDiaryUseCase {
        return SaveDiaryUseCase(
            diaryRepository,
            takePersistableUriPermissionUseCase,
            releasePersistableUriPermissionUseCase
        )
    }

    @Singleton
    @Provides
    fun provideDeleteDiaryUseCase(
        diaryRepository: DiaryRepository,
        releasePersistableUriPermissionUseCase: ReleasePersistableUriPermissionUseCase,
    ): DeleteDiaryUseCase {
        return DeleteDiaryUseCase(
            diaryRepository,
            releasePersistableUriPermissionUseCase
        )
    }

    @Singleton
    @Provides
    fun provideShouldFetchWeatherInfoUseCase(): ShouldFetchWeatherInfoUseCase {
        return ShouldFetchWeatherInfoUseCase()
    }

    @Singleton
    @Provides
    fun provideShouldRequestExitWithoutDiarySaveConfirmationUseCase() =
        ShouldRequestExitWithoutDiarySaveConfirmationUseCase()

    @Singleton
    @Provides
    fun provideLoadDiaryListUseCase(diaryRepository: DiaryRepository) =
        LoadDiaryListUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideCountDiariesUseCase(diaryRepository: DiaryRepository) =
        CountDiariesUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideCheckUnloadedDiariesExistUseCase(countDiariesUseCase:CountDiariesUseCase) =
        CheckUnloadedDiariesExistUseCase(countDiariesUseCase)

    @Singleton
    @Provides
    fun provideLoadNewestDiaryUseCase(diaryRepository: DiaryRepository) =
        LoadNewestDiaryUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideLoadOldestDiaryUseCase(diaryRepository: DiaryRepository): LoadOldestDiaryUseCase =
        LoadOldestDiaryUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideLoadWordSearchResultDiaryListUseCase(diaryRepository: DiaryRepository) =
        LoadWordSearchResultDiaryListUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideCountWordSearchResultDiariesUseCase(diaryRepository: DiaryRepository) =
        CountWordSearchResultDiariesUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideCheckUnloadedWordSearchResultDiariesExistUseCase(
        countWordSearchResultDiariesUseCase: CountWordSearchResultDiariesUseCase
    ) = CheckUnloadedWordSearchResultDiariesExistUseCase(countWordSearchResultDiariesUseCase)

    @Singleton
    @Provides
    fun provideDeleteDiaryItemTitleSelectionHistoryItemUseCase(
        diaryRepository: DiaryRepository
    ) = DeleteDiaryItemTitleSelectionHistoryItemUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideLoadDiaryItemTitleSelectionHistoryUseCase(
        diaryRepository: DiaryRepository
    ) = LoadDiaryItemTitleSelectionHistoryUseCase(diaryRepository)
}
