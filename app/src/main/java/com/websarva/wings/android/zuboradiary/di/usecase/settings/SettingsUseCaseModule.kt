package com.websarva.wings.android.zuboradiary.di.usecase.settings

import com.websarva.wings.android.zuboradiary.data.repository.UserPreferencesRepository
import com.websarva.wings.android.zuboradiary.data.usecase.settings.IsWeatherInfoAcquisitionEnabledUseCase
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
    fun provideIsWeatherInfoAcquisitionEnabledUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): IsWeatherInfoAcquisitionEnabledUseCase {
        return IsWeatherInfoAcquisitionEnabledUseCase(userPreferencesRepository)
    }
}
