package com.websarva.wings.android.zuboradiary.worker;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReminderNotificationWorker extends Worker {
    private final NotificationManager notificationManager;
    private final String CHANNEL_ID = "reminder_notification_worker";
    private final String CHANNEL_NAME = "リマインダー通知"; // TODO:string.xmlへ置換
    private final String CHANNEL_DESCRIPTION = "設定した時間に日記を書く事をお知らせします。"; // TODO:string.xmlへ置換
    private final int NOTIFY_ID = 100;
    public ReminderNotificationWorker(
            @NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        notificationManager =
                (NotificationManager)
                        getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // TODO:Ver.対策を考える。
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @NonNull
    @Override
    public Result doWork() {
        Notification notification =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon()
                        .setContentTitle("お疲れ様です") // TODO:string.xmlへ置換
                        .setContentText("今日の出来事を日記に書きましょう。") // TODO:string.xmlへ置換
                        .build();
        boolean isPermission =
                ActivityCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED;
        if (isPermission) {
            notificationManager.notify(NOTIFY_ID, notification);
            return Result.success();
        } else {
            return Result.failure();
        }
    }
}
