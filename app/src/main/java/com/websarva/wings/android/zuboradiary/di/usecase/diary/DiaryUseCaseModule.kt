package com.websarva.wings.android.zuboradiary.di.usecase.diary

import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.WeatherInfoRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CanFetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CheckUnloadedDiariesExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CheckUnloadedWordSearchResultsExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CountDiariesUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CountWordSearchResultsUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryItemTitleSelectionHistoryItemUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryItemTitleSelectionHistoryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadNewestDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadOldestDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryUpdateConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.FetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadAdditionDiaryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadAdditionWordSearchResultListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadNewDiaryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadNewWordSearchResultListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadWordSearchResultListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.RefreshDiaryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.RefreshWordSearchResultListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.SaveDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldFetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryLoadConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestExitWithoutDiarySaveConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestWeatherInfoConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.UpdateDiaryListFooterUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.UpdateWordSearchResultListFooterUseCase
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
    fun provideCanFetchWeatherInfoUseCase(
        weatherInfoRepository: WeatherInfoRepository
    ): CanFetchWeatherInfoUseCase {
        return CanFetchWeatherInfoUseCase(weatherInfoRepository)
    }

    @Singleton
    @Provides
    fun provideCheckUnloadedDiariesExistUseCase(countDiariesUseCase: CountDiariesUseCase) =
        CheckUnloadedDiariesExistUseCase(countDiariesUseCase)

    @Singleton
    @Provides
    fun provideCheckUnloadedWordSearchResultsExistUseCase(
        countWordSearchResultsUseCase: CountWordSearchResultsUseCase
    ) = CheckUnloadedWordSearchResultsExistUseCase(countWordSearchResultsUseCase)

    @Singleton
    @Provides
    fun provideCountDiariesUseCase(diaryRepository: DiaryRepository) =
        CountDiariesUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideCountWordSearchResultsUseCase(diaryRepository: DiaryRepository) =
        CountWordSearchResultsUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideDeleteDiaryItemTitleSelectionHistoryItemUseCase(
        diaryRepository: DiaryRepository
    ) = DeleteDiaryItemTitleSelectionHistoryItemUseCase(diaryRepository)

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
    fun provideDoesDiaryExistUseCase(
        diaryRepository: DiaryRepository
    ): DoesDiaryExistUseCase {
        return DoesDiaryExistUseCase(diaryRepository)
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
    fun provideLoadAdditionDiaryListUseCase(
        loadDiaryListUseCase: LoadDiaryListUseCase,
        updateDiaryListFooterUseCase: UpdateDiaryListFooterUseCase
    ) = LoadAdditionDiaryListUseCase(loadDiaryListUseCase, updateDiaryListFooterUseCase)

    @Singleton
    @Provides
    fun provideLoadAdditionWordSearchResultListUseCase(
        loadWordSearchResultListUseCase: LoadWordSearchResultListUseCase,
        updateWordSearchResultListFooterUseCase: UpdateWordSearchResultListFooterUseCase
    ) = LoadAdditionWordSearchResultListUseCase(
        loadWordSearchResultListUseCase,
        updateWordSearchResultListFooterUseCase
    )

    @Singleton
    @Provides
    fun provideLoadDiaryItemTitleSelectionHistoryListUseCase(
        diaryRepository: DiaryRepository
    ) = LoadDiaryItemTitleSelectionHistoryListUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideLoadDiaryListUseCase(
        diaryRepository: DiaryRepository
    ) = LoadDiaryListUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideLoadDiaryUseCase(
        diaryRepository: DiaryRepository
    ): LoadDiaryUseCase {
        return LoadDiaryUseCase(diaryRepository)
    }

    @Singleton
    @Provides
    fun provideLoadNewDiaryListUseCase(
        loadDiaryListUseCase: LoadDiaryListUseCase,
        updateDiaryListFooterUseCase: UpdateDiaryListFooterUseCase
    ) = LoadNewDiaryListUseCase(loadDiaryListUseCase, updateDiaryListFooterUseCase)

    @Singleton
    @Provides
    fun provideLoadNewestDiaryUseCase(diaryRepository: DiaryRepository) =
        LoadNewestDiaryUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideLoadNewWordSearchResultListUseCase(
        loadWordSearchResultListUseCase: LoadWordSearchResultListUseCase,
        updateWordSearchResultListFooterUseCase: UpdateWordSearchResultListFooterUseCase
    ) = LoadNewWordSearchResultListUseCase(
        loadWordSearchResultListUseCase,
        updateWordSearchResultListFooterUseCase
    )

    @Singleton
    @Provides
    fun provideLoadOldestDiaryUseCase(diaryRepository: DiaryRepository): LoadOldestDiaryUseCase =
        LoadOldestDiaryUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideLoadWordSearchResultListUseCase(
        diaryRepository: DiaryRepository
    ) = LoadWordSearchResultListUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideRefreshDiaryListUseCase(
        loadDiaryListUseCase: LoadDiaryListUseCase,
        updateDiaryListFooterUseCase: UpdateDiaryListFooterUseCase
    ) = RefreshDiaryListUseCase(loadDiaryListUseCase, updateDiaryListFooterUseCase)

    @Singleton
    @Provides
    fun provideRefreshWordSearchResultListUseCase(
        loadWordSearchResultListUseCase: LoadWordSearchResultListUseCase,
        updateWordSearchResultListFooterUseCase: UpdateWordSearchResultListFooterUseCase
    ) = RefreshWordSearchResultListUseCase(
        loadWordSearchResultListUseCase,
        updateWordSearchResultListFooterUseCase
    )

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
    fun provideShouldFetchWeatherInfoUseCase(): ShouldFetchWeatherInfoUseCase {
        return ShouldFetchWeatherInfoUseCase()
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
    fun provideShouldRequestExitWithoutDiarySaveConfirmationUseCase() =
        ShouldRequestExitWithoutDiarySaveConfirmationUseCase()

    @Singleton
    @Provides
    fun provideShouldRequestWeatherInfoConfirmationUseCase(
        shouldFetchWeatherInfoUseCase: ShouldFetchWeatherInfoUseCase
    ): ShouldRequestWeatherInfoConfirmationUseCase {
        return ShouldRequestWeatherInfoConfirmationUseCase(shouldFetchWeatherInfoUseCase)
    }

    @Singleton
    @Provides
    fun provideUpdateDiaryListFooterUseCase(
        checkUnloadedDiariesExistUseCase: CheckUnloadedDiariesExistUseCase
    ) = UpdateDiaryListFooterUseCase(checkUnloadedDiariesExistUseCase)

    @Singleton
    @Provides
    fun provideUpdateWordSearchResultListFooterUseCase(
        checkUnloadedWordSearchResultsExistUseCase: CheckUnloadedWordSearchResultsExistUseCase
    ) = UpdateWordSearchResultListFooterUseCase(checkUnloadedWordSearchResultsExistUseCase)
}
