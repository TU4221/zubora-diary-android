package com.websarva.wings.android.zuboradiary.worker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.hilt.work.HiltWorker;
import androidx.navigation.NavDeepLinkBuilder;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.websarva.wings.android.zuboradiary.CustomApplication;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;

import java.time.LocalDate;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class ReminderNotificationWorker extends Worker {
    private NotificationManager notificationManager;
    private final String CHANNEL_ID = "reminder_notification_worker";
    private final String CHANNEL_NAME = "リマインダー通知"; // TODO:string.xmlへ置換
    private final String CHANNEL_DESCRIPTION = "設定した時間に日記を書く事をお知らせします。"; // TODO:string.xmlへ置換
    private final int NOTIFY_ID = 100;
    private final DiaryRepository diaryRepository;
    @AssistedInject
    public ReminderNotificationWorker(
            @Assisted /*@NonNull*/ Context context,
            @Assisted /*@NonNull*/ WorkerParameters workerParams,
            DiaryRepository diaryRepository) {
        super(context, workerParams);
        this.diaryRepository = diaryRepository;
        prepareNotificationManager();
    }

    @NonNull
    @Override
    public Result doWork() {
        Boolean isAppInForeground = isAppInForeground();
        if (isAppInForeground == null) {
            return Result.failure();
        }
        if (isAppInForeground) {
            return Result.success();
        }
        boolean hasWriteTodayDiary;
        try {
            hasWriteTodayDiary = hasWriteTodayDiary();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
        if (hasWriteTodayDiary) {
            return Result.success();
        }
        return showHeadsUpNotification();
    }

    private void prepareNotificationManager() {
        notificationManager =
                (NotificationManager)
                        getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // MEMO:NotificationChannelはSdk26以降の機能
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

    private Boolean isAppInForeground() {
        CustomApplication application = null;
        if (getApplicationContext() instanceof CustomApplication) {
            Log.d("NotificationWorker", "isAppInForeground()_isCustomApplication:" + String.valueOf(true));
            application = (CustomApplication) getApplicationContext();
        } else {
            Log.d("NotificationWorker", "isAppInForeground()_isCustomApplication:" + String.valueOf(false));
            return null;
        }
        Log.d("NotificationWorker", "isAppInForeground()_isAppInForeground:" + String.valueOf(application.getIsAppInForeground()));
        return application.getIsAppInForeground();
    }

    private boolean hasWriteTodayDiary() throws Exception{
        LocalDate today = LocalDate.now();
        boolean result =
                diaryRepository.hasDiary(today.getYear(), today.getMonthValue(), today.getDayOfMonth());
        Log.d("NotificationWorker", "hasWriteTodayDiary():" + String.valueOf(result));
        return result;
    }

    private Result showHeadsUpNotification() {
        boolean isPermission =
                ActivityCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED;
        Log.d("NotificationWorker", "showHeadsUpNotification()_NotificationIsPermission:" + String.valueOf(isPermission));

        if (!isPermission) {
            return Result.failure();
        }

        PendingIntent pendingIntent =
                new NavDeepLinkBuilder(getApplicationContext())
                        .setGraph(R.navigation.mobile_navigation)
                        .setDestination(R.id.navigation_diary_list_fragment)
                        .createPendingIntent();

        NotificationCompat.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext());
        }
        Notification notification =
                builder.setSmallIcon(R.drawable.ic_notifications_24px)
                        .setContentTitle("お疲れ様です") // TODO:string.xmlへ置換
                        .setContentText("今日の出来事を日記に書きましょう。") // TODO:string.xmlへ置換
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this.getApplicationContext());
        notificationManager.notify(NOTIFY_ID, notification);
        return Result.success();
    }
}
