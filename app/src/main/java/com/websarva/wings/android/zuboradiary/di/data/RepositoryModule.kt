package com.websarva.wings.android.zuboradiary.di.data

import com.websarva.wings.android.zuboradiary.data.database.DiaryDataSource
import com.websarva.wings.android.zuboradiary.data.location.FusedLocationDataSource
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiDataSource
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.repository.WeatherInfoRepository
import com.websarva.wings.android.zuboradiary.data.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesDataSource
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import com.websarva.wings.android.zuboradiary.data.repository.SchedulingRepository
import com.websarva.wings.android.zuboradiary.data.repository.WeatherInfoRepositoryImpl
import com.websarva.wings.android.zuboradiary.data.uri.UriPermissionDataSource
import com.websarva.wings.android.zuboradiary.data.worker.NotificationSchedulingDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object RepositoryModule {

    @Singleton
    @Provides
    fun provideDiaryRepository(
        diaryDataSource: DiaryDataSource
    ): DiaryRepository = DiaryRepository(diaryDataSource)

    @Singleton
    @Provides
    fun provideSettingsRepository(
        userPreferencesDataSource: UserPreferencesDataSource
    ): SettingsRepository = SettingsRepository(userPreferencesDataSource)

    @Singleton
    @Provides
    fun provideSchedulingRepository(
        workManager: NotificationSchedulingDataSource
    ): SchedulingRepository = SchedulingRepository(workManager)

    @Singleton
    @Provides
    fun provideWeatherApiRepository(
        weatherApiDataSource: WeatherApiDataSource,
        fusedLocationDataSource: FusedLocationDataSource
    ): WeatherInfoRepository =
        WeatherInfoRepositoryImpl(
            weatherApiDataSource,
            fusedLocationDataSource
        )

    @Singleton
    @Provides
    fun provideUriRepository(
        uriPermissionDataSource: UriPermissionDataSource
    ): UriRepository = UriRepository(uriPermissionDataSource)
}
