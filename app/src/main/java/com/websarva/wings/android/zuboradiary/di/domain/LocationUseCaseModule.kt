package com.websarva.wings.android.zuboradiary.di.domain

import com.websarva.wings.android.zuboradiary.domain.repository.LocationRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.location.FetchCurrentLocationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 位置情報取得関連のユースケースの依存性を提供するHiltモジュール。
 *
 * このモジュールは、[SingletonComponent] にインストールされ、
 * アプリケーション全体で共有されるシングルトンインスタンスを提供する。
 *
 * 各インスタンスは、対応する `@Provides` アノテーションが付与されたメソッドによって生成される。
 */
@Module
@InstallIn(SingletonComponent::class)
internal object LocationUseCaseModule {

    @Singleton
    @Provides
    fun provideFetchCurrentLocationUseCase(
        locationRepository: LocationRepository
    ): FetchCurrentLocationUseCase =
        FetchCurrentLocationUseCase(locationRepository)
}
