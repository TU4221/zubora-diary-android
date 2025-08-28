package com.websarva.wings.android.zuboradiary.di.data

import android.content.Context
import androidx.work.WorkManager
import com.websarva.wings.android.zuboradiary.data.worker.NotificationSchedulingDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object WorkerModule {

    @Singleton
    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Singleton
    @Provides
    fun provideNotificationSchedulingDataSource(
        workManager: WorkManager
    ): NotificationSchedulingDataSource = NotificationSchedulingDataSource(workManager)
}
