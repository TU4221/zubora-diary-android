package com.websarva.wings.android.zuboradiary.di.usecase.uri

import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleaseAllUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleaseUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.TakeUriPermissionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object UriUseCaseModule {

    @Singleton
    @Provides
    fun provideReleaseAllUriPermissionUseCase(
        uriRepository: UriRepository
    ): ReleaseAllUriPermissionUseCase {
        return ReleaseAllUriPermissionUseCase(uriRepository)
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
}
