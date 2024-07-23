package com.websarva.wings.android.zuboradiary.worker;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavDeepLinkBuilder;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.websarva.wings.android.zuboradiary.CustomApplication;
import com.websarva.wings.android.zuboradiary.R;

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
                    new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @NonNull
    @Override
    public Result doWork() {
        CustomApplication application = null;
        if (getApplicationContext() instanceof CustomApplication) {
            Log.d("20240723", "application != null");
            application = (CustomApplication) getApplicationContext();
        } else {
            return Result.failure();
        }

        if (application != null && application.getIsAppInForeground()) {
            Log.d("20240723", "isAppInForeground == true");
            return Result.failure();
        }
        Log.d("20240723", "isAppInForeground == false");

        NotificationCompat.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext());
        }

        PendingIntent pendingIntent =
                new NavDeepLinkBuilder(getApplicationContext())
                        .setGraph(R.navigation.mobile_navigation)
                        .setDestination(R.id.navigation_diary_list_fragment)
                        .createPendingIntent();

        Notification notification =
                builder.setSmallIcon(R.drawable.ic_notifications_24px)
                        .setContentTitle("お疲れ様です") // TODO:string.xmlへ置換
                        .setContentText("今日の出来事を日記に書きましょう。") // TODO:string.xmlへ置換
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();
        boolean isPermission =
                ActivityCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED;
        Log.d("20240711", "NotificationIsPermission:" + String.valueOf(isPermission));
        if (isPermission) {
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this.getApplicationContext());
            notificationManager.notify(NOTIFY_ID, notification);
            return Result.success();
        } else {
            return Result.failure();
        }
    }
}
