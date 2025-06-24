package com.websarva.wings.android.zuboradiary.di

import com.websarva.wings.android.zuboradiary.data.database.DiaryDAO
import com.websarva.wings.android.zuboradiary.data.database.DiaryDatabase
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryDAO
import com.websarva.wings.android.zuboradiary.data.repository.DiaryItemTitleSelectionHistoryRepository
import com.websarva.wings.android.zuboradiary.data.location.FusedLocationDataSource
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiDataSource
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.WeatherApiRepository
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferences
import com.websarva.wings.android.zuboradiary.data.repository.LocationRepository
import com.websarva.wings.android.zuboradiary.data.repository.UriRepository
import com.websarva.wings.android.zuboradiary.data.repository.WorkerRepository
import com.websarva.wings.android.zuboradiary.data.uri.UriPermissionDataSource
import com.websarva.wings.android.zuboradiary.data.worker.ReminderNotificationWorkManager
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
        diaryDatabase: DiaryDatabase,
        diaryDAO: DiaryDAO
    ): DiaryRepository {
        return DiaryRepository(diaryDatabase, diaryDAO)
    }

    @Singleton
    @Provides
    fun provideEditDiarySelectItemTitleRepository(
        diaryItemTitleSelectionHistoryDAO: DiaryItemTitleSelectionHistoryDAO
    ): DiaryItemTitleSelectionHistoryRepository {
        return DiaryItemTitleSelectionHistoryRepository(diaryItemTitleSelectionHistoryDAO)
    }

    @Singleton
    @Provides
    fun provideUserPreferencesRepository(userPreferences: UserPreferences): UserPreferencesRepository {
        return UserPreferencesRepository(userPreferences)
    }

    @Singleton
    @Provides
    fun provideWorkerRepository(workManager: ReminderNotificationWorkManager): WorkerRepository {
        return WorkerRepository(workManager)
    }

    @Singleton
    @Provides
    fun provideWeatherApiRepository(weatherApiDataSource: WeatherApiDataSource): WeatherApiRepository {
        return WeatherApiRepository(weatherApiDataSource)
    }

    @Singleton
    @Provides
    fun provideLocationRepository(fusedLocationDataSource: FusedLocationDataSource): LocationRepository {
        return LocationRepository(fusedLocationDataSource)
    }

    @Singleton
    @Provides
    fun provideUriRepository(
        uriPermissionDataSource: UriPermissionDataSource
    ): UriRepository {
        return UriRepository(uriPermissionDataSource)
    }
}
