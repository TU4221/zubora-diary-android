package com.websarva.wings.android.zuboradiary.di.data

import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.repository.WeatherInfoRepository
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepositoryImpl
import com.websarva.wings.android.zuboradiary.data.repository.FileRepositoryImpl
import com.websarva.wings.android.zuboradiary.data.repository.LocationRepositoryImpl
import com.websarva.wings.android.zuboradiary.data.repository.SchedulingRepositoryImpl
import com.websarva.wings.android.zuboradiary.data.repository.SettingsRepositoryImpl
import com.websarva.wings.android.zuboradiary.domain.repository.SchedulingRepository
import com.websarva.wings.android.zuboradiary.data.repository.WeatherInfoRepositoryImpl
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.repository.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * リポジトリ関連の依存性を提供するHiltモジュール。
 *
 * このモジュールは、[SingletonComponent] にインストールされ、
 * アプリケーション全体で共有されるシングルトンインスタンスを提供する。
 *
 * 各インターフェースは、対応する実装クラスと `@Binds` アノテーションによって紐付けられる。
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    // Hiltが内部的に参照するため、IDE上の未使用警告は無視
    @Suppress("unused")
    @Singleton
    @Binds
    abstract fun bindDiaryRepository(
        impl: DiaryRepositoryImpl
    ): DiaryRepository

    // Hiltが内部的に参照するため、IDE上の未使用警告は無視
    @Suppress("unused")
    @Singleton
    @Binds
    abstract fun bindLocationRepository(
        impl: LocationRepositoryImpl
    ): LocationRepository

    // Hiltが内部的に参照するため、IDE上の未使用警告は無視
    @Suppress("unused")
    @Singleton
    @Binds
    abstract fun bindFileRepository(
        impl: FileRepositoryImpl
    ): FileRepository

    // Hiltが内部的に参照するため、IDE上の未使用警告は無視
    @Suppress("unused")
    @Singleton
    @Binds
    abstract fun bindSchedulingRepository(
        impl: SchedulingRepositoryImpl
    ): SchedulingRepository

    // Hiltが内部的に参照するため、IDE上の未使用警告は無視
    @Suppress("unused")
    @Singleton
    @Binds
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    // Hiltが内部的に参照するため、IDE上の未使用警告は無視
    @Suppress("unused")
    @Singleton
    @Binds
    abstract fun bindWeatherApiRepository(
        impl: WeatherInfoRepositoryImpl
    ): WeatherInfoRepository
}
