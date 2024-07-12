package com.websarva.wings.android.zuboradiary.data.worker;

import android.app.Application;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.websarva.wings.android.zuboradiary.worker.ReminderNotificationWorker;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class WorkerRepository {
    private WorkManager workManager;
    private final String WORK_TAG_REMINDER_NOTIFICATION = "ReminderNotification";
    private final String UNIQUE_WORK_NAME_REMINDER_NOTIFICATION = WORK_TAG_REMINDER_NOTIFICATION;
    public WorkerRepository(Application application) {
        workManager = WorkManager.getInstance(application.getApplicationContext());
    }

    public void registerReminderNotificationWorker(LocalTime settingTime) {
        workManager.cancelAllWork();
        LocalTime nowTime = LocalTime.now();
        long initialDelaySeconds = calculationBetweenSeconds(nowTime, settingTime);
        Log.d("20240711", "initialDelaySeconds:" + String.valueOf(initialDelaySeconds));
        PeriodicWorkRequest request =
                new PeriodicWorkRequest
                        .Builder(ReminderNotificationWorker.class, 15, TimeUnit.MINUTES)
                        .addTag(WORK_TAG_REMINDER_NOTIFICATION)
                        .setInitialDelay(initialDelaySeconds, TimeUnit.SECONDS)
                        .build();
        workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME_REMINDER_NOTIFICATION, ExistingPeriodicWorkPolicy.UPDATE, request);
    }

    private long calculationBetweenSeconds(LocalTime startTime, LocalTime endTime) {
        long betweenSeconds = ChronoUnit.SECONDS.between(startTime, endTime);
        if (betweenSeconds < 0) {
            return 60/*seconds*/ * 60/*minutes*/ * 24/*hours*/ + betweenSeconds;
        }
        return betweenSeconds;
    }

    public void cancelReminderNotificationWorker() {
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME_REMINDER_NOTIFICATION);
    }
}
