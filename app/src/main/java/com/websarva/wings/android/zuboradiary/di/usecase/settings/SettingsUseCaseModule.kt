package com.websarva.wings.android.zuboradiary.di.usecase.settings

import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.data.repository.WorkerRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.DeleteAllDataUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.DeleteAllDiariesUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadCalendarStartDayOfWeekSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadPasscodeLockSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadReminderNotificationSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadThemeColorSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadWeatherInfoFetchSettingUseCase
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
    fun provideInitializeAllSettingsUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): InitializeAllSettingsUseCase {
        return InitializeAllSettingsUseCase(userPreferencesRepository)
    }

    @Singleton
    @Provides
    fun provideIsWeatherInfoFetchEnabledUseCase(
        loadWeatherInfoFetchSettingUseCase: LoadWeatherInfoFetchSettingUseCase
    ): IsWeatherInfoFetchEnabledUseCase {
        return IsWeatherInfoFetchEnabledUseCase(loadWeatherInfoFetchSettingUseCase)
    }

    @Singleton
    @Provides
    fun provideLoadCalendarStartDayOfWeekSettingUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): LoadCalendarStartDayOfWeekSettingUseCase {
        return LoadCalendarStartDayOfWeekSettingUseCase(userPreferencesRepository)
    }

    @Singleton
    @Provides
    fun provideLoadPasscodeLockSettingUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): LoadPasscodeLockSettingUseCase {
        return LoadPasscodeLockSettingUseCase(userPreferencesRepository)
    }

    @Singleton
    @Provides
    fun provideLoadReminderNotificationSettingUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): LoadReminderNotificationSettingUseCase {
        return LoadReminderNotificationSettingUseCase(userPreferencesRepository)
    }

    @Singleton
    @Provides
    fun provideLoadThemeColorSettingUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): LoadThemeColorSettingUseCase {
        return LoadThemeColorSettingUseCase(userPreferencesRepository)
    }

    @Singleton
    @Provides
    fun provideLoadWeatherInfoFetchSettingUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): LoadWeatherInfoFetchSettingUseCase {
        return LoadWeatherInfoFetchSettingUseCase(userPreferencesRepository)
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
        loadReminderNotificationSettingUseCase: LoadReminderNotificationSettingUseCase
    ): SaveReminderNotificationSettingUseCase {
        return SaveReminderNotificationSettingUseCase(
            userPreferencesRepository,
            workerRepository,
            loadReminderNotificationSettingUseCase
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
