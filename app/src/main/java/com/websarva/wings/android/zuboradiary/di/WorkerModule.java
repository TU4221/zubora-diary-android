package com.websarva.wings.android.zuboradiary.di;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;

import com.websarva.wings.android.zuboradiary.data.database.DiaryDAO;
import com.websarva.wings.android.zuboradiary.data.database.DiaryDatabase;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;
import com.websarva.wings.android.zuboradiary.worker.ReminderNotificationWorker;

import java.util.Objects;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ActivityContext;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoMap;

@Module
@InstallIn(SingletonComponent.class)
public class WorkerModule {
    @Singleton
    @Provides
    @NonNull
    public static WorkManager provideWorkManager(@ApplicationContext Context context) {
        Objects.requireNonNull(context);

        return WorkManager.getInstance(context);
    }
}
