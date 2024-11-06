package com.websarva.wings.android.zuboradiary.di;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;

import com.websarva.wings.android.zuboradiary.data.database.DiaryDAO;
import com.websarva.wings.android.zuboradiary.data.database.DiaryDatabase;
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryRepository;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.data.database.DiaryItemTitleSelectionHistoryDAO;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiRepository;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiResponse;
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiService;
import com.websarva.wings.android.zuboradiary.data.preferences.SettingsRepository;
import com.websarva.wings.android.zuboradiary.data.preferences.UserPreferences;
import com.websarva.wings.android.zuboradiary.data.worker.WorkerRepository;

import java.util.Objects;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {
    @Singleton
    @Provides
    @NonNull
    public static DiaryRepository provideDiaryRepository(
            DiaryDatabase diaryDatabase,
            DiaryDAO diaryDAO,
            DiaryItemTitleSelectionHistoryDAO diaryItemTitleSelectionHistoryDAO) {
        Objects.requireNonNull(diaryDatabase);
        Objects.requireNonNull(diaryDAO);
        Objects.requireNonNull(diaryItemTitleSelectionHistoryDAO);

        DiaryRepository repository =
                new DiaryRepository(diaryDatabase, diaryDAO, diaryItemTitleSelectionHistoryDAO);
        return Objects.requireNonNull(repository);
    }

    @Singleton
    @Provides
    @NonNull
    public static DiaryItemTitleSelectionHistoryRepository provideEditDiarySelectItemTitleRepository(
            DiaryItemTitleSelectionHistoryDAO diaryItemTitleSelectionHistoryDAO) {
        Objects.requireNonNull(diaryItemTitleSelectionHistoryDAO);

        DiaryItemTitleSelectionHistoryRepository repository =
                new DiaryItemTitleSelectionHistoryRepository(diaryItemTitleSelectionHistoryDAO);
        return Objects.requireNonNull(repository);
    }

    @Singleton
    @Provides
    @NonNull
    public static SettingsRepository provideSettingsRepository(UserPreferences userPreferences) {
        Objects.requireNonNull(userPreferences);

        SettingsRepository repository = new SettingsRepository(userPreferences);
        return Objects.requireNonNull(repository);
    }

    @Singleton
    @Provides
    @NonNull
    public static WorkerRepository provideWorkerRepository(WorkManager workManager) {
        Objects.requireNonNull(workManager);

        WorkerRepository repository = new WorkerRepository(workManager);
        return Objects.requireNonNull(repository);
    }

    @Singleton
    @Provides
    @NonNull
    public static WeatherApiRepository provideWeatherApiRepository(WeatherApiService weatherApiService) {
        Objects.requireNonNull(weatherApiService);

        WeatherApiRepository repository = new WeatherApiRepository(weatherApiService);
        return Objects.requireNonNull(repository);
    }
}
