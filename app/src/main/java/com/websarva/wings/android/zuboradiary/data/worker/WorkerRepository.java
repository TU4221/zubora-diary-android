package com.websarva.wings.android.zuboradiary.data.worker;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.gms.common.internal.ConnectionTelemetryConfiguration;
import com.websarva.wings.android.zuboradiary.CustomApplication;
import com.websarva.wings.android.zuboradiary.worker.ReminderNotificationWorker;
import com.websarva.wings.android.zuboradiary.worker.TestWorker;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class WorkerRepository {
    private WorkManager workManager;
    private final String WORK_TAG_REMINDER_NOTIFICATION = "ReminderNotification";
    private final String UNIQUE_WORK_NAME_REMINDER_NOTIFICATION = WORK_TAG_REMINDER_NOTIFICATION;
    @Inject
    public WorkerRepository(WorkManager workManager) {
        this.workManager = workManager;
    }

    public void registerReminderNotificationWorker(LocalTime settingTime) {
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME_REMINDER_NOTIFICATION);
        workManager.cancelAllWorkByTag(WORK_TAG_REMINDER_NOTIFICATION);
        LocalTime nowTime = LocalTime.now();
        long initialDelaySeconds = calculationBetweenSeconds(nowTime, settingTime);
        Log.d("NotificationWorker", "initialDelaySeconds:" + String.valueOf(initialDelaySeconds));
        PeriodicWorkRequest request =
                new PeriodicWorkRequest
                        .Builder(ReminderNotificationWorker.class, 15, TimeUnit.MINUTES)
                        .addTag(WORK_TAG_REMINDER_NOTIFICATION)
                        .setInitialDelay(initialDelaySeconds, TimeUnit.SECONDS)
                        .build();
        workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME_REMINDER_NOTIFICATION, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request);
        // TODO:下記は調整後削除
        /*OneTimeWorkRequest request2 = OneTimeWorkRequest.from(ReminderNotificationWorker.class);
        workManager.enqueue(request2);*/
        /*try {
            List<WorkInfo> list = workManager.getWorkInfosByTag(WORK_TAG_REMINDER_NOTIFICATION).get();
            Log.d("20240726", String.valueOf(list.size()));
            for(WorkInfo i: list) {
                WorkInfo.State[] states = WorkInfo.State.values();
                for (WorkInfo.State state: states) {
                    if (i.getState() == state) {
                        Log.d("20240726", i.toString());
                        Log.d("20240726", state.name());
                    }
                }
            }
            workManager.cancelAllWorkByTag(WORK_TAG_REMINDER_NOTIFICATION);
            List<WorkInfo> _list = workManager.getWorkInfosForUniqueWork(WORK_TAG_REMINDER_NOTIFICATION).get();
            Log.d("20240726", String.valueOf(_list.size()));
            for(WorkInfo i: _list) {
                WorkInfo.State[] states = WorkInfo.State.values();
                for (WorkInfo.State state: states) {
                    if (i.getState() == state) {
                        Log.d("20240726", i.toString());
                        Log.d("20240726", state.name());
                    }
                }
            }
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME_REMINDER_NOTIFICATION);
        } catch (ExecutionException | InterruptedException e) {

        }*/

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
