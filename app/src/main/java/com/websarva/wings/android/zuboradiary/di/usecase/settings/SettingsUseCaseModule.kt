package com.websarva.wings.android.zuboradiary.di.usecase.settings

import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.data.repository.WorkerRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.DeleteAllDataUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.DeleteAllDiariesUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.FetchAllSettingsValueUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.InitializeAllSettingsUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.IsWeatherInfoFetchEnabledUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveCalendarStartDayOfWeekUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SavePasscodeLockSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveReminderNotificationSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveThemeColorSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveWeatherInfoFetchSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.uri.ReleaseAllUriPermissionUseCase
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
        releaseAllUriPermissionUseCase: ReleaseAllUriPermissionUseCase
    ): DeleteAllDataUseCase {
        return DeleteAllDataUseCase(diaryRepository, releaseAllUriPermissionUseCase)
    }

    @Singleton
    @Provides
    fun provideDeleteAllDiariesUseCase(
        diaryRepository: DiaryRepository,
        releaseAllUriPermissionUseCase: ReleaseAllUriPermissionUseCase
    ): DeleteAllDiariesUseCase {
        return DeleteAllDiariesUseCase(diaryRepository, releaseAllUriPermissionUseCase)
    }

    @Singleton
    @Provides
    fun provideFetchAllSettingsValueUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): FetchAllSettingsValueUseCase {
        return FetchAllSettingsValueUseCase(userPreferencesRepository)
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
        userPreferencesRepository: UserPreferencesRepository
    ): IsWeatherInfoFetchEnabledUseCase {
        return IsWeatherInfoFetchEnabledUseCase(userPreferencesRepository)
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
        workerRepository: WorkerRepository
    ): SaveReminderNotificationSettingUseCase {
        return SaveReminderNotificationSettingUseCase(userPreferencesRepository, workerRepository)
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
