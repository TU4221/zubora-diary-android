package com.websarva.wings.android.zuboradiary.di;

import android.content.Context;

import androidx.work.WorkManager;

import com.websarva.wings.android.zuboradiary.data.database.DiaryDAO;
import com.websarva.wings.android.zuboradiary.data.database.DiaryDatabase;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.data.database.EditDiarySelectItemTitleRepository;
import com.websarva.wings.android.zuboradiary.data.database.SelectedItemTitlesHistoryDAO;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiRepository;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiService;
import com.websarva.wings.android.zuboradiary.data.settings.SettingsRepository;
import com.websarva.wings.android.zuboradiary.data.settings.UserPreferences;
import com.websarva.wings.android.zuboradiary.data.worker.WorkerRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {
    @Singleton
    @Provides
    public static DiaryRepository provideDiaryRepository(
            @ApplicationContext Context context, DiaryDatabase diaryDatabase, DiaryDAO diaryDAO) {
        return new DiaryRepository(context, diaryDatabase, diaryDAO);
    }

    @Singleton
    @Provides
    public static EditDiarySelectItemTitleRepository provideEditDiarySelectItemTitleRepository(
            SelectedItemTitlesHistoryDAO selectedItemTitlesHistoryDAO) {
        return new EditDiarySelectItemTitleRepository(selectedItemTitlesHistoryDAO);
    }

    @Singleton
    @Provides
    public static SettingsRepository provideSettingsRepository(
            UserPreferences userPreferences) {
        return new SettingsRepository(userPreferences);
    }

    @Singleton
    @Provides
    public static WorkerRepository provideWorkerRepository(WorkManager workManager) {
        return new WorkerRepository(workManager);
    }

    @Singleton
    @Provides
    public static WeatherApiRepository provideWeatherApiRepository(
            WeatherApiService weatherApiService) {
        return new WeatherApiRepository(weatherApiService);
    }
}
