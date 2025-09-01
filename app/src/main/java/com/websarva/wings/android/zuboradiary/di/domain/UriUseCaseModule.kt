package com.websarva.wings.android.zuboradiary.di.domain

import com.websarva.wings.android.zuboradiary.domain.repository.UriRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleaseAllPersistableUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleasePersistableUriPermissionUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.TakePersistableUriPermissionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * URI権限関連のユースケースの依存性を提供するHiltモジュール。
 *
 * このモジュールは、[SingletonComponent] にインストールされ、
 * アプリケーション全体で共有されるシングルトンインスタンスを提供する。
 *
 * 各インスタンスは、対応する `@Provides` アノテーションが付与されたメソッドによって生成される。
 */
@Module
@InstallIn(SingletonComponent::class)
internal object UriUseCaseModule {

    @Singleton
    @Provides
    fun provideReleaseAllPersistableUriPermissionUseCase(
        uriRepository: UriRepository
    ): ReleaseAllPersistableUriPermissionUseCase =
        ReleaseAllPersistableUriPermissionUseCase(uriRepository)

    @Singleton
    @Provides
    fun provideReleasePersistableUriPermissionUseCase(
        uriRepository: UriRepository
    ): ReleasePersistableUriPermissionUseCase =
        ReleasePersistableUriPermissionUseCase(uriRepository)

    @Singleton
    @Provides
    fun provideTakePersistableUriPermissionUseCase(
        uriRepository: UriRepository
    ): TakePersistableUriPermissionUseCase =
        TakePersistableUriPermissionUseCase(uriRepository)
}
