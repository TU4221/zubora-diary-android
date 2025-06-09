package com.websarva.wings.android.zuboradiary.di

import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import com.websarva.wings.android.zuboradiary.data.usecase.diary.CheckDiaryExistsUseCase
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
    ): CheckDiaryExistsUseCase {
        return CheckDiaryExistsUseCase(diaryRepository)
    }

    @Singleton
    @Provides
    fun provideReleaseUriPermissionUseCase(
        uriRepository: UriRepository,
        diaryRepository: DiaryRepository
    ): ReleaseUriPermissionUseCase {
        return ReleaseUriPermissionUseCase(uriRepository, diaryRepository)
    }
}
