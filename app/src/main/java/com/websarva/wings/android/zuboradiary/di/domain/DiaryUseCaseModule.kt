package com.websarva.wings.android.zuboradiary.di.domain

import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CheckUnloadedDiariesExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CheckUnloadedWordSearchResultsExistUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CountDiariesUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CountWordSearchResultsUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CreateDiaryImageFileUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryItemTitleSelectionHistoryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.IsImageUriUsedInDiariesUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryItemTitleSelectionHistoryListUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadNewestDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadOldestDiaryUseCase
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
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ReleaseDiaryImageUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.SaveDiaryUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldFetchWeatherInfoUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryLoadConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestExitWithoutDiarySaveConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestWeatherInfoConfirmationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.UpdateDiaryListFooterUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.UpdateWordSearchResultListFooterUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.file.SaveImageFileUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleasePersistableUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.TakePersistableUriPermissionUseCase
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
    fun provideCheckUnloadedDiariesExistUseCase(
        countDiariesUseCase: CountDiariesUseCase
    ): CheckUnloadedDiariesExistUseCase = CheckUnloadedDiariesExistUseCase(countDiariesUseCase)

    @Singleton
    @Provides
    fun provideCheckUnloadedWordSearchResultsExistUseCase(
        countWordSearchResultsUseCase: CountWordSearchResultsUseCase
    ): CheckUnloadedWordSearchResultsExistUseCase =
        CheckUnloadedWordSearchResultsExistUseCase(countWordSearchResultsUseCase)

    @Singleton
    @Provides
    fun provideCountDiariesUseCase(
        diaryRepository: DiaryRepository
    ): CountDiariesUseCase = CountDiariesUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideCountWordSearchResultsUseCase(
        diaryRepository: DiaryRepository
    ): CountWordSearchResultsUseCase = CountWordSearchResultsUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideCreateDiaryImageFileUseCase(
        saveImageFileUseCase: SaveImageFileUseCase
    ): CreateDiaryImageFileUseCase = CreateDiaryImageFileUseCase(saveImageFileUseCase)

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
        releaseDiaryImageUriPermissionUseCase: ReleaseDiaryImageUriPermissionUseCase
    ): DeleteDiaryUseCase =
        DeleteDiaryUseCase(diaryRepository, releaseDiaryImageUriPermissionUseCase)

    @Singleton
    @Provides
    fun provideDoesDiaryExistUseCase(
        diaryRepository: DiaryRepository
    ): DoesDiaryExistUseCase = DoesDiaryExistUseCase(diaryRepository)

    @Singleton
    @Provides
    fun provideIsImageUriUsedInDiariesUseCase(
        diaryRepository: DiaryRepository
    ): IsImageUriUsedInDiariesUseCase = IsImageUriUsedInDiariesUseCase(diaryRepository)

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
        loadNewestDiaryUseCase: LoadNewestDiaryUseCase,
        loadOldestDiaryUseCase: LoadOldestDiaryUseCase
    ): LoadDiaryListStartYearMonthPickerDateRangeUseCase =
        LoadDiaryListStartYearMonthPickerDateRangeUseCase(
            loadNewestDiaryUseCase,
            loadOldestDiaryUseCase
        )

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
    fun provideLoadNewestDiaryUseCase(
        diaryRepository: DiaryRepository
    ): LoadNewestDiaryUseCase = LoadNewestDiaryUseCase(diaryRepository)

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
    fun provideLoadOldestDiaryUseCase(
        diaryRepository: DiaryRepository
    ): LoadOldestDiaryUseCase = LoadOldestDiaryUseCase(diaryRepository)

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
    fun provideReleaseDiaryImageUriPermissionUseCase(
        isImageUriUsedInDiariesUseCase: IsImageUriUsedInDiariesUseCase,
        releasePersistableUriPermissionUseCase: ReleasePersistableUriPermissionUseCase
    ): ReleaseDiaryImageUriPermissionUseCase =
        ReleaseDiaryImageUriPermissionUseCase(
            isImageUriUsedInDiariesUseCase,
            releasePersistableUriPermissionUseCase
        )

    @Singleton
    @Provides
    fun provideSaveDiaryUseCase(
        diaryRepository: DiaryRepository,
        takePersistableUriPermissionUseCase: TakePersistableUriPermissionUseCase,
        releaseDiaryImageUriPermissionUseCase: ReleaseDiaryImageUriPermissionUseCase
    ): SaveDiaryUseCase =
        SaveDiaryUseCase(
            diaryRepository,
            takePersistableUriPermissionUseCase,
            releaseDiaryImageUriPermissionUseCase
        )

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
        checkUnloadedDiariesExistUseCase: CheckUnloadedDiariesExistUseCase
    ): UpdateDiaryListFooterUseCase =
        UpdateDiaryListFooterUseCase(checkUnloadedDiariesExistUseCase)

    @Singleton
    @Provides
    fun provideUpdateWordSearchResultListFooterUseCase(
        checkUnloadedWordSearchResultsExistUseCase: CheckUnloadedWordSearchResultsExistUseCase
    ): UpdateWordSearchResultListFooterUseCase =
        UpdateWordSearchResultListFooterUseCase(checkUnloadedWordSearchResultsExistUseCase)
}
