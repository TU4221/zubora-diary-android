package com.websarva.wings.android.zuboradiary.di.domain

import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.BuildDiaryImageFilePathUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CacheDiaryImageUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ClearDiaryImageCacheFileUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CountWordSearchResultsUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryItemTitleSelectionHistoryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryItemTitleSelectionHistoryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryUpdateConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadAdditionDiaryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadAdditionWordSearchResultListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryListStartYearMonthPickerDateRangeUseCase
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 日記関連のユースケースの依存性を提供するHiltモジュール。
 *
 * このモジュールは、[SingletonComponent] にインストールされ、
 * アプリケーション全体で共有されるシングルトンインスタンスを提供する。
 *
 * 各インスタンスは、対応する `@Provides` アノテーションが付与されたメソッドによって生成される。
 */
@Module
@InstallIn(SingletonComponent::class)
internal object DiaryUseCaseModule {

    @Singleton
    @Provides
    fun provideBuildDiaryImageFilePathUseCase(
        fileRepository: FileRepository
    ): BuildDiaryImageFilePathUseCase = BuildDiaryImageFilePathUseCase(fileRepository)

    @Singleton
    @Provides
    fun provideCacheDiaryImageUseCase(
        fileRepository: FileRepository
    ): CacheDiaryImageUseCase = CacheDiaryImageUseCase(fileRepository)

    @Singleton
    @Provides
    fun provideClearDiaryImageCacheFileUseCase(
        fileRepository: FileRepository
    ): ClearDiaryImageCacheFileUseCase = ClearDiaryImageCacheFileUseCase(fileRepository)

    @Singleton
    @Provides
    fun provideCountWordSearchResultsUseCase(
        diaryRepository: DiaryRepository
    ): CountWordSearchResultsUseCase = CountWordSearchResultsUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideDeleteDiaryItemTitleSelectionHistoryUseCase(
        diaryRepository: DiaryRepository
    ): DeleteDiaryItemTitleSelectionHistoryUseCase =
        DeleteDiaryItemTitleSelectionHistoryUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideDeleteDiaryUseCase(
        diaryRepository: DiaryRepository,
        fileRepository: FileRepository
    ): DeleteDiaryUseCase =
        DeleteDiaryUseCase(diaryRepository, fileRepository)

    @Singleton
    @Provides
    fun provideDoesDiaryExistUseCase(
        diaryRepository: DiaryRepository
    ): DoesDiaryExistUseCase = DoesDiaryExistUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideLoadAdditionDiaryListUseCase(
        loadDiaryListUseCase: LoadDiaryListUseCase,
        updateDiaryListFooterUseCase: UpdateDiaryListFooterUseCase
    ): LoadAdditionDiaryListUseCase =
        LoadAdditionDiaryListUseCase(loadDiaryListUseCase, updateDiaryListFooterUseCase)

    @Singleton
    @Provides
    fun provideLoadAdditionWordSearchResultListUseCase(
        loadWordSearchResultListUseCase: LoadWordSearchResultListUseCase,
        updateWordSearchResultListFooterUseCase: UpdateWordSearchResultListFooterUseCase
    ): LoadAdditionWordSearchResultListUseCase =
        LoadAdditionWordSearchResultListUseCase(
            loadWordSearchResultListUseCase,
            updateWordSearchResultListFooterUseCase
        )

    @Singleton
    @Provides
    fun provideLoadDiaryItemTitleSelectionHistoryListUseCase(
        diaryRepository: DiaryRepository
    ): LoadDiaryItemTitleSelectionHistoryListUseCase =
        LoadDiaryItemTitleSelectionHistoryListUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideLoadDiaryListStartYearMonthPickerDateRange(
        diaryRepository: DiaryRepository
    ): LoadDiaryListStartYearMonthPickerDateRangeUseCase =
        LoadDiaryListStartYearMonthPickerDateRangeUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideLoadDiaryListUseCase(
        diaryRepository: DiaryRepository
    ): LoadDiaryListUseCase = LoadDiaryListUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideLoadDiaryUseCase(
        diaryRepository: DiaryRepository
    ): LoadDiaryUseCase = LoadDiaryUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideLoadNewDiaryListUseCase(
        loadDiaryListUseCase: LoadDiaryListUseCase,
        updateDiaryListFooterUseCase: UpdateDiaryListFooterUseCase
    ): LoadNewDiaryListUseCase =
        LoadNewDiaryListUseCase(loadDiaryListUseCase, updateDiaryListFooterUseCase)

    @Singleton
    @Provides
    fun provideLoadNewWordSearchResultListUseCase(
        loadWordSearchResultListUseCase: LoadWordSearchResultListUseCase,
        updateWordSearchResultListFooterUseCase: UpdateWordSearchResultListFooterUseCase
    ): LoadNewWordSearchResultListUseCase =
        LoadNewWordSearchResultListUseCase(
            loadWordSearchResultListUseCase,
            updateWordSearchResultListFooterUseCase
        )

    @Singleton
    @Provides
    fun provideLoadWordSearchResultListUseCase(
        diaryRepository: DiaryRepository
    ): LoadWordSearchResultListUseCase = LoadWordSearchResultListUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideRefreshDiaryListUseCase(
        loadDiaryListUseCase: LoadDiaryListUseCase,
        updateDiaryListFooterUseCase: UpdateDiaryListFooterUseCase
    ): RefreshDiaryListUseCase =
        RefreshDiaryListUseCase(loadDiaryListUseCase, updateDiaryListFooterUseCase)

    @Singleton
    @Provides
    fun provideRefreshWordSearchResultListUseCase(
        loadWordSearchResultListUseCase: LoadWordSearchResultListUseCase,
        updateWordSearchResultListFooterUseCase: UpdateWordSearchResultListFooterUseCase
    ): RefreshWordSearchResultListUseCase =
        RefreshWordSearchResultListUseCase(
            loadWordSearchResultListUseCase,
            updateWordSearchResultListFooterUseCase
        )

    @Singleton
    @Provides
    fun provideSaveDiaryUseCase(
        diaryRepository: DiaryRepository,
        fileRepository: FileRepository
    ): SaveDiaryUseCase = SaveDiaryUseCase(diaryRepository, fileRepository)

    @Singleton
    @Provides
    fun provideShouldFetchWeatherInfoUseCase(): ShouldFetchWeatherInfoUseCase =
        ShouldFetchWeatherInfoUseCase()

    @Singleton
    @Provides
    fun provideShouldRequestDiaryLoadConfirmationUseCase(
        doesDiaryExistUseCase: DoesDiaryExistUseCase
    ): ShouldRequestDiaryLoadConfirmationUseCase =
        ShouldRequestDiaryLoadConfirmationUseCase(doesDiaryExistUseCase)

    @Singleton
    @Provides
    fun provideShouldRequestDiaryUpdateConfirmationUseCase(
        doesDiaryExistUseCase: DoesDiaryExistUseCase
    ): ShouldRequestDiaryUpdateConfirmationUseCase =
        ShouldRequestDiaryUpdateConfirmationUseCase(doesDiaryExistUseCase)

    @Singleton
    @Provides
    fun provideShouldRequestExitWithoutDiarySaveConfirmationUseCase():
            ShouldRequestExitWithoutDiarySaveConfirmationUseCase =
                ShouldRequestExitWithoutDiarySaveConfirmationUseCase()

    @Singleton
    @Provides
    fun provideShouldRequestWeatherInfoConfirmationUseCase(
        shouldFetchWeatherInfoUseCase: ShouldFetchWeatherInfoUseCase
    ): ShouldRequestWeatherInfoConfirmationUseCase =
        ShouldRequestWeatherInfoConfirmationUseCase(shouldFetchWeatherInfoUseCase)

    @Singleton
    @Provides
    fun provideUpdateDiaryListFooterUseCase(
        diaryRepository: DiaryRepository
    ): UpdateDiaryListFooterUseCase =
        UpdateDiaryListFooterUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideUpdateWordSearchResultListFooterUseCase(
        countWordSearchResultsUseCase: CountWordSearchResultsUseCase
    ): UpdateWordSearchResultListFooterUseCase =
        UpdateWordSearchResultListFooterUseCase(countWordSearchResultsUseCase)
}
