package com.websarva.wings.android.zuboradiary.di

import androidx.work.WorkManager
import com.websarva.wings.android.zuboradiary.data.database.DiaryDAO
import com.websarva.wings.android.zuboradiary.data.database.DiaryDatabase
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryDAO
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryRepository
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiRepository
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiService
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferences
import com.websarva.wings.android.zuboradiary.data.worker.WorkerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @JvmStatic
    @Singleton
    @Provides
    fun provideDiaryRepository(
        diaryDatabase: DiaryDatabase,
        diaryDAO: DiaryDAO,
        diaryItemTitleSelectionHistoryDAO: DiaryItemTitleSelectionHistoryDAO
    ): DiaryRepository {
        return DiaryRepository(diaryDatabase, diaryDAO, diaryItemTitleSelectionHistoryDAO)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideEditDiarySelectItemTitleRepository(
        diaryItemTitleSelectionHistoryDAO: DiaryItemTitleSelectionHistoryDAO
    ): DiaryItemTitleSelectionHistoryRepository {
        return DiaryItemTitleSelectionHistoryRepository(diaryItemTitleSelectionHistoryDAO)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideUserPreferencesRepository(userPreferences: UserPreferences): UserPreferencesRepository {
        return UserPreferencesRepository(userPreferences)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideWorkerRepository(workManager: WorkManager): WorkerRepository {
        return WorkerRepository(workManager)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideWeatherApiRepository(weatherApiService: WeatherApiService): WeatherApiRepository {
        return WeatherApiRepository(weatherApiService)
    }
}
