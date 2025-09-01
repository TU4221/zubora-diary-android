package com.websarva.wings.android.zuboradiary.di.domain

import com.websarva.wings.android.zuboradiary.domain.repository.SchedulingRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.scheduling.CancelReminderNotificationUseCase
import com.websarva.wings.android.zuboradiary.domain.usecase.scheduling.RegisterReminderNotificationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * スケジューリング関連のユースケースの依存性を提供するHiltモジュール。
 *
 * このモジュールは、[SingletonComponent] にインストールされ、
 * アプリケーション全体で共有されるシングルトンインスタンスを提供する。
 *
 * 各インスタンスは、対応する `@Provides` アノテーションが付与されたメソッドによって生成される。
 */
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
