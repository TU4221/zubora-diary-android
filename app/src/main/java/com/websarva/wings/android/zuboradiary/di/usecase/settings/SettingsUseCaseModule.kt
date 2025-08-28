package com.websarva.wings.android.zuboradiary.di.usecase.settings

import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.data.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.scheduling.CancelReminderNotificationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.scheduling.RegisterReminderNotificationUseCase
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
    ): DeleteAllDataUseCase =
        DeleteAllDataUseCase(
            diaryRepository,
            releaseAllPersistableUriPermissionUseCase,
            initializeAllSettingsUseCase
        )

    @Singleton
    @Provides
    fun provideDeleteAllDiariesUseCase(
        diaryRepository: DiaryRepository,
        releaseAllPersistableUriPermissionUseCase: ReleaseAllPersistableUriPermissionUseCase
    ): DeleteAllDiariesUseCase =
        DeleteAllDiariesUseCase(
            diaryRepository,
            releaseAllPersistableUriPermissionUseCase
        )

    @Singleton
    @Provides
    fun provideInitializeAllSettingsUseCase(
        settingsRepository: SettingsRepository
    ): InitializeAllSettingsUseCase =
        InitializeAllSettingsUseCase(settingsRepository)

    @Singleton
    @Provides
    fun provideIsWeatherInfoFetchEnabledUseCase(
        loadWeatherInfoFetchSettingUseCase: LoadWeatherInfoFetchSettingUseCase
    ): IsWeatherInfoFetchEnabledUseCase =
        IsWeatherInfoFetchEnabledUseCase(loadWeatherInfoFetchSettingUseCase)

    @Singleton
    @Provides
    fun provideLoadCalendarStartDayOfWeekSettingUseCase(
        settingsRepository: SettingsRepository
    ): LoadCalendarStartDayOfWeekSettingUseCase =
        LoadCalendarStartDayOfWeekSettingUseCase(settingsRepository)

    @Singleton
    @Provides
    fun provideLoadPasscodeLockSettingUseCase(
        settingsRepository: SettingsRepository
    ): LoadPasscodeLockSettingUseCase =
        LoadPasscodeLockSettingUseCase(settingsRepository)

    @Singleton
    @Provides
    fun provideLoadReminderNotificationSettingUseCase(
        settingsRepository: SettingsRepository
    ): LoadReminderNotificationSettingUseCase =
        LoadReminderNotificationSettingUseCase(settingsRepository)

    @Singleton
    @Provides
    fun provideLoadThemeColorSettingUseCase(
        settingsRepository: SettingsRepository
    ): LoadThemeColorSettingUseCase =
        LoadThemeColorSettingUseCase(settingsRepository)

    @Singleton
    @Provides
    fun provideLoadWeatherInfoFetchSettingUseCase(
        settingsRepository: SettingsRepository
    ): LoadWeatherInfoFetchSettingUseCase =
        LoadWeatherInfoFetchSettingUseCase(settingsRepository)

    @Singleton
    @Provides
    fun provideSaveCalendarStartDayOfWeekUseCase(
        settingsRepository: SettingsRepository
    ): SaveCalendarStartDayOfWeekUseCase =
        SaveCalendarStartDayOfWeekUseCase(settingsRepository)

    @Singleton
    @Provides
    fun provideSavePasscodeLockSettingUseCase(
        settingsRepository: SettingsRepository
    ): SavePasscodeLockSettingUseCase =
        SavePasscodeLockSettingUseCase(settingsRepository)

    @Singleton
    @Provides
    fun provideSaveReminderNotificationSettingUseCase(
        settingsRepository: SettingsRepository,
        loadReminderNotificationSettingUseCase: LoadReminderNotificationSettingUseCase,
        registerReminderNotificationUseCase: RegisterReminderNotificationUseCase,
        cancelReminderNotificationUseCase: CancelReminderNotificationUseCase
    ): SaveReminderNotificationSettingUseCase =
        SaveReminderNotificationSettingUseCase(
            settingsRepository,
            loadReminderNotificationSettingUseCase,
            registerReminderNotificationUseCase,
            cancelReminderNotificationUseCase
        )

    @Singleton
    @Provides
    fun provideSaveThemeColorSettingUseCase(
        settingsRepository: SettingsRepository
    ): SaveThemeColorSettingUseCase =
        SaveThemeColorSettingUseCase(settingsRepository)

    @Singleton
    @Provides
    fun provideSaveWeatherInfoFetchSettingUseCase(
        settingsRepository: SettingsRepository
    ): SaveWeatherInfoFetchSettingUseCase =
        SaveWeatherInfoFetchSettingUseCase(settingsRepository)
}
