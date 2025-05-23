package com.websarva.wings.android.zuboradiary.di

import androidx.work.WorkManager
import com.websarva.wings.android.zuboradiary.data.database.DiaryDAO
import com.websarva.wings.android.zuboradiary.data.database.DiaryDatabase
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryDAO
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryRepository
import com.websarva.wings.android.zuboradiary.data.location.FusedLocationDataSource
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.WeatherApiRepository
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiService
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferences
import com.websarva.wings.android.zuboradiary.data.repository.LocationRepository
import com.websarva.wings.android.zuboradiary.data.repository.WorkerRepository
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
    fun provideWorkerRepository(workManager: WorkManager): WorkerRepository {
        return WorkerRepository(workManager)
    }

    @Singleton
    @Provides
    fun provideWeatherApiRepository(weatherApiService: WeatherApiService): WeatherApiRepository {
        return WeatherApiRepository(weatherApiService)
    }

    @Singleton
    @Provides
    fun provideLocationRepository(fusedLocationDataSource: FusedLocationDataSource): LocationRepository {
        return LocationRepository(fusedLocationDataSource)
    }
}
