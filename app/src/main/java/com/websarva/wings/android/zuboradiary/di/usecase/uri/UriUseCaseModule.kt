package com.websarva.wings.android.zuboradiary.di.usecase.uri

import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleaseAllPersistableUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleasePersistableUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.TakePersistableUriPermissionUseCase
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
    fun provideReleaseAllPersistableUriPermissionUseCase(
        uriRepository: UriRepository
    ): ReleaseAllPersistableUriPermissionUseCase {
        return ReleaseAllPersistableUriPermissionUseCase(uriRepository)
    }

    @Singleton
    @Provides
    fun provideReleasePersistableUriPermissionUseCase(
        uriRepository: UriRepository,
        diaryRepository: DiaryRepository
    ): ReleasePersistableUriPermissionUseCase {
        return ReleasePersistableUriPermissionUseCase(uriRepository, diaryRepository)
    }

    @Singleton
    @Provides
    fun provideTakePersistableUriPermissionUseCase(
        uriRepository: UriRepository
    ): TakePersistableUriPermissionUseCase {
        return TakePersistableUriPermissionUseCase(uriRepository)
    }
}
