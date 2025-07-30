package com.websarva.wings.android.zuboradiary.di.usecase.settings

import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.data.repository.WorkerRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.DeleteAllDataUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.DeleteAllDiariesUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.FetchCalendarStartDayOfWeekSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.FetchPasscodeLockSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.FetchReminderNotificationSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.FetchThemeColorSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.FetchWeatherInfoFetchSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.InitializeAllSettingsUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.IsWeatherInfoFetchEnabledUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveCalendarStartDayOfWeekUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SavePasscodeLockSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveReminderNotificationSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveThemeColorSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveWeatherInfoFetchSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleaseAllPersistableUriPermissionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object SettingsUseCaseModule {
    @Singleton
    @Provides
    fun provideDeleteAllDataUseCase(
        diaryRepository: DiaryRepository,
        releaseAllPersistableUriPermissionUseCase: ReleaseAllPersistableUriPermissionUseCase,
        initializeAllSettingsUseCase: InitializeAllSettingsUseCase
    ): DeleteAllDataUseCase {
        return DeleteAllDataUseCase(
            diaryRepository,
            releaseAllPersistableUriPermissionUseCase,
            initializeAllSettingsUseCase
        )
    }

    @Singleton
    @Provides
    fun provideDeleteAllDiariesUseCase(
        diaryRepository: DiaryRepository,
        releaseAllPersistableUriPermissionUseCase: ReleaseAllPersistableUriPermissionUseCase
    ): DeleteAllDiariesUseCase {
        return DeleteAllDiariesUseCase(diaryRepository, releaseAllPersistableUriPermissionUseCase)
    }

    @Singleton
    @Provides
    fun provideFetchCalendarStartDayOfWeekSettingUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): FetchCalendarStartDayOfWeekSettingUseCase {
        return FetchCalendarStartDayOfWeekSettingUseCase(userPreferencesRepository)
    }

    @Singleton
    @Provides
    fun provideFetchPasscodeLockSettingUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): FetchPasscodeLockSettingUseCase {
        return FetchPasscodeLockSettingUseCase(userPreferencesRepository)
    }

    @Singleton
    @Provides
    fun provideFetchReminderNotificationSettingUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): FetchReminderNotificationSettingUseCase {
        return FetchReminderNotificationSettingUseCase(userPreferencesRepository)
    }

    @Singleton
    @Provides
    fun provideFetchThemeColorSettingUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): FetchThemeColorSettingUseCase {
        return FetchThemeColorSettingUseCase(userPreferencesRepository)
    }

    @Singleton
    @Provides
    fun provideFetchWeatherInfoFetchSettingUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): FetchWeatherInfoFetchSettingUseCase {
        return FetchWeatherInfoFetchSettingUseCase(userPreferencesRepository)
    }

    @Singleton
    @Provides
    fun provideInitializeAllSettingsUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): InitializeAllSettingsUseCase {
        return InitializeAllSettingsUseCase(userPreferencesRepository)
    }

    @Singleton
    @Provides
    fun provideIsWeatherInfoFetchEnabledUseCase(
        fetchWeatherInfoFetchSettingUseCase: FetchWeatherInfoFetchSettingUseCase
    ): IsWeatherInfoFetchEnabledUseCase {
        return IsWeatherInfoFetchEnabledUseCase(fetchWeatherInfoFetchSettingUseCase)
    }

    @Singleton
    @Provides
    fun provideSaveCalendarStartDayOfWeekUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): SaveCalendarStartDayOfWeekUseCase {
        return SaveCalendarStartDayOfWeekUseCase(userPreferencesRepository)
    }

    @Singleton
    @Provides
    fun provideSavePasscodeLockSettingUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): SavePasscodeLockSettingUseCase {
        return SavePasscodeLockSettingUseCase(userPreferencesRepository)
    }

    @Singleton
    @Provides
    fun provideSaveReminderNotificationSettingUseCase(
        userPreferencesRepository: UserPreferencesRepository,
        workerRepository: WorkerRepository,
        fetchReminderNotificationSettingUseCase: FetchReminderNotificationSettingUseCase
    ): SaveReminderNotificationSettingUseCase {
        return SaveReminderNotificationSettingUseCase(
            userPreferencesRepository,
            workerRepository,
            fetchReminderNotificationSettingUseCase
        )
    }

    @Singleton
    @Provides
    fun provideSaveThemeColorSettingUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): SaveThemeColorSettingUseCase {
        return SaveThemeColorSettingUseCase(userPreferencesRepository)
    }

    @Singleton
    @Provides
    fun provideSaveWeatherInfoFetchSettingUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): SaveWeatherInfoFetchSettingUseCase {
        return SaveWeatherInfoFetchSettingUseCase(userPreferencesRepository)
    }
}
