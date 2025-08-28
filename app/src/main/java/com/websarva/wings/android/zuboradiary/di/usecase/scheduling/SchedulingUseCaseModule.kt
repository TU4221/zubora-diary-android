package com.websarva.wings.android.zuboradiary.di.usecase.scheduling

import com.websarva.wings.android.zuboradiary.data.repository.SchedulingRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.scheduling.CancelReminderNotificationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.scheduling.RegisterReminderNotificationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object SchedulingUseCaseModule {

    @Singleton
    @Provides
    fun provideRegisterReminderNotificationUseCase(
        schedulingRepository: SchedulingRepository
    ): RegisterReminderNotificationUseCase =
        RegisterReminderNotificationUseCase(schedulingRepository)

    @Singleton
    @Provides
    fun provideCancelReminderNotificationUseCase(
        schedulingRepository: SchedulingRepository
    ): CancelReminderNotificationUseCase =
        CancelReminderNotificationUseCase(schedulingRepository)
}
