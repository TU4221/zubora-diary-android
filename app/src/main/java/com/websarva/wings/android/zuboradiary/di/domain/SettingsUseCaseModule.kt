package com.websarva.wings.android.zuboradiary.di.domain

import com.websarva.wings.android.zuboradiary.domain.repository.DiaryRepository
import com.websarva.wings.android.zuboradiary.domain.repository.FileRepository
import com.websarva.wings.android.zuboradiary.domain.repository.SchedulingRepository
import com.websarva.wings.android.zuboradiary.domain.repository.SettingsRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.DeleteAllDataUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteAllDiariesUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadCalendarStartDayOfWeekSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadPasscodeLockSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadReminderNotificationSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadThemeColorSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadWeatherInfoFetchSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.InitializeAllSettingsUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.CheckWeatherInfoFetchEnabledUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.UpdateCalendarStartDayOfWeekSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.UpdatePasscodeLockSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.UpdateReminderNotificationSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.UpdateThemeColorSettingUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.UpdateWeatherInfoFetchSettingUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 設定関連のユースケースの依存性を提供するHiltモジュール。
 *
 * このモジュールは、[SingletonComponent] にインストールされ、
 * アプリケーション全体で共有されるシングルトンインスタンスを提供する。
 *
 * 各インスタンスは、対応する `@Provides` アノテーションが付与されたメソッドによって生成される。
 */
@Module
@InstallIn(SingletonComponent::class)
internal object SettingsUseCaseModule {

    @Singleton
    @Provides
    fun provideDeleteAllDataUseCase(
        diaryRepository: DiaryRepository,
        fileRepository: FileRepository,
        initializeAllSettingsUseCase: InitializeAllSettingsUseCase
    ): DeleteAllDataUseCase =
        DeleteAllDataUseCase(
            diaryRepository,
            fileRepository,
            initializeAllSettingsUseCase
        )

    @Singleton
    @Provides
    fun provideDeleteAllDiariesUseCase(
        diaryRepository: DiaryRepository,
        fileRepository: FileRepository
    ): DeleteAllDiariesUseCase =
        DeleteAllDiariesUseCase(
            diaryRepository,
            fileRepository
        )

    @Singleton
    @Provides
    fun provideInitializeAllSettingsUseCase(
        updateThemeColorSettingUseCase: UpdateThemeColorSettingUseCase,
        updateCalendarStartDayOfWeekSettingUseCase: UpdateCalendarStartDayOfWeekSettingUseCase,
        updateReminderNotificationSettingUseCase: UpdateReminderNotificationSettingUseCase,
        updatePasscodeLockSettingUseCase: UpdatePasscodeLockSettingUseCase,
        updateWeatherInfoFetchSettingUseCase: UpdateWeatherInfoFetchSettingUseCase
    ): InitializeAllSettingsUseCase =
        InitializeAllSettingsUseCase(
            updateThemeColorSettingUseCase,
            updateCalendarStartDayOfWeekSettingUseCase,
            updateReminderNotificationSettingUseCase,
            updatePasscodeLockSettingUseCase,
            updateWeatherInfoFetchSettingUseCase
        )

    @Singleton
    @Provides
    fun provideCheckWeatherInfoFetchEnabledUseCase(
        loadWeatherInfoFetchSettingUseCase: LoadWeatherInfoFetchSettingUseCase
    ): CheckWeatherInfoFetchEnabledUseCase =
        CheckWeatherInfoFetchEnabledUseCase(loadWeatherInfoFetchSettingUseCase)

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
    fun provideUpdateCalendarStartDayOfWeekSettingUseCase(
        settingsRepository: SettingsRepository
    ): UpdateCalendarStartDayOfWeekSettingUseCase =
        UpdateCalendarStartDayOfWeekSettingUseCase(settingsRepository)

    @Singleton
    @Provides
    fun provideUpdatePasscodeLockSettingUseCase(
        settingsRepository: SettingsRepository
    ): UpdatePasscodeLockSettingUseCase =
        UpdatePasscodeLockSettingUseCase(settingsRepository)

    @Singleton
    @Provides
    fun provideUpdateReminderNotificationSettingUseCase(
        settingsRepository: SettingsRepository,
        schedulingRepository: SchedulingRepository,
        loadReminderNotificationSettingUseCase: LoadReminderNotificationSettingUseCase
    ): UpdateReminderNotificationSettingUseCase =
        UpdateReminderNotificationSettingUseCase(
            settingsRepository,
            schedulingRepository,
            loadReminderNotificationSettingUseCase
        )

    @Singleton
    @Provides
    fun provideUpdateThemeColorSettingUseCase(
        settingsRepository: SettingsRepository
    ): UpdateThemeColorSettingUseCase =
        UpdateThemeColorSettingUseCase(settingsRepository)

    @Singleton
    @Provides
    fun provideUpdateWeatherInfoFetchSettingUseCase(
        settingsRepository: SettingsRepository
    ): UpdateWeatherInfoFetchSettingUseCase =
        UpdateWeatherInfoFetchSettingUseCase(settingsRepository)
}
