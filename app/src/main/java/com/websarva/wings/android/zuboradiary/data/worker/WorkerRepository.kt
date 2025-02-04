package com.websarva.wings.android.zuboradiary.data.worker;

import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.websarva.wings.android.zuboradiary.worker.ReminderNotificationWorker;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class WorkerRepository {

    private final WorkManager workManager;
    private final String WORK_TAG_REMINDER_NOTIFICATION = "ReminderNotification";
    private final String UNIQUE_WORK_NAME_REMINDER_NOTIFICATION = WORK_TAG_REMINDER_NOTIFICATION;

    @Inject
    public WorkerRepository(WorkManager workManager) {
        Objects.requireNonNull(workManager);

        this.workManager = workManager;
    }

    public void registerReminderNotificationWorker(LocalTime settingTime) {
        Objects.requireNonNull(settingTime);

        cancelReminderNotificationWorker();

        LocalTime nowTime = LocalTime.now();
        long initialDelaySeconds = calculationBetweenSeconds(nowTime, settingTime);
        Log.d("NotificationWorker", "initialDelaySeconds:" + initialDelaySeconds);
        PeriodicWorkRequest request =
                new PeriodicWorkRequest
                        .Builder(ReminderNotificationWorker.class, 1, TimeUnit.DAYS)
                        .addTag(WORK_TAG_REMINDER_NOTIFICATION)
                        .setInitialDelay(initialDelaySeconds, TimeUnit.SECONDS)
                        .build();
        workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME_REMINDER_NOTIFICATION, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request);
    }

    private long calculationBetweenSeconds(LocalTime startTime, LocalTime endTime) {
        long betweenSeconds = ChronoUnit.SECONDS.between(startTime, endTime);
        if (betweenSeconds < 0) {
            return 60/*seconds*/ * 60/*minutes*/ * 24/*hours*/ + betweenSeconds;
        }
        return betweenSeconds;
    }

    public void cancelReminderNotificationWorker() {
        workManager.cancelAllWorkByTag(WORK_TAG_REMINDER_NOTIFICATION);
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME_REMINDER_NOTIFICATION);
    }
}
