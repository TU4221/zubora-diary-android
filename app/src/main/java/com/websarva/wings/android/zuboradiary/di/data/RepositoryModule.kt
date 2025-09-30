package com.websarva.wings.android.zuboradiary.di.data

import com.websarva.wings.android.zuboradiary.data.database.DiaryDataSource
import com.websarva.wings.android.zuboradiary.data.file.ImageFileDataSource
import com.websarva.wings.android.zuboradiary.data.location.FusedLocationDataSource
import com.websarva.wings.android.zuboradiary.data.mapper.diary.DiaryRepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.mapper.file.FileRepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.mapper.location.LocationRepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.mapper.scheduling.SchedulingRepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.mapper.settings.SettingsRepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.mapper.weather.WeatherInfoRepositoryExceptionMapper
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiDataSource
import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.repository.WeatherInfoRepository
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesDataSource
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepositoryImpl
import com.websarva.wings.android.zuboradiary.data.repository.FileRepositoryImpl
import com.websarva.wings.android.zuboradiary.data.repository.LocationRepositoryImpl
import com.websarva.wings.android.zuboradiary.data.repository.SchedulingRepositoryImpl
import com.websarva.wings.android.zuboradiary.data.repository.SettingsRepositoryImpl
import com.websarva.wings.android.zuboradiary.domain.repository.SchedulingRepository
import com.websarva.wings.android.zuboradiary.data.repository.WeatherInfoRepositoryImpl
import com.websarva.wings.android.zuboradiary.data.worker.NotificationSchedulingDataSource
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.repository.LocationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * リポジトリ関連の依存性を提供するHiltモジュール。
 *
 * このモジュールは、[SingletonComponent] にインストールされ、
 * アプリケーション全体で共有されるシングルトンインスタンスを提供する。
 *
 * 各インスタンスは、対応する `@Provides` アノテーションが付与されたメソッドによって生成される。
 */
@Module
@InstallIn(SingletonComponent::class)
internal object RepositoryModule {

    @Singleton
    @Provides
    fun provideDiaryRepositoryImpl(
        diaryDataSource: DiaryDataSource
    ): DiaryRepositoryImpl =
        DiaryRepositoryImpl(diaryDataSource, DiaryRepositoryExceptionMapper)

    @Singleton
    @Provides
    fun provideDiaryRepository(
        diaryRepositoryImpl: DiaryRepositoryImpl
    ): DiaryRepository = diaryRepositoryImpl

    @Singleton
    @Provides
    fun provideLocationRepository(
        fusedLocationDataSource: FusedLocationDataSource
    ): LocationRepository =
        LocationRepositoryImpl(fusedLocationDataSource, LocationRepositoryExceptionMapper)

    @Singleton
    @Provides
    fun provideFileRepository(
        imageFileDataSource: ImageFileDataSource
    ): FileRepository = FileRepositoryImpl(imageFileDataSource, FileRepositoryExceptionMapper)

    @Singleton
    @Provides
    fun provideSchedulingRepository(
        workManager: NotificationSchedulingDataSource
    ): SchedulingRepository =
        SchedulingRepositoryImpl(
            workManager,
            SchedulingRepositoryExceptionMapper
            )

    @Singleton
    @Provides
    fun provideSettingsRepository(
        userPreferencesDataSource: UserPreferencesDataSource
    ): SettingsRepository =
        SettingsRepositoryImpl(userPreferencesDataSource, SettingsRepositoryExceptionMapper)

    @Singleton
    @Provides
    fun provideWeatherApiRepository(
        weatherApiDataSource: WeatherApiDataSource
    ): WeatherInfoRepository =
        WeatherInfoRepositoryImpl(weatherApiDataSource, WeatherInfoRepositoryExceptionMapper)
}
