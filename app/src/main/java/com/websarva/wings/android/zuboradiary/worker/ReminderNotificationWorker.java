package com.websarva.wings.android.zuboradiary.worker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.hilt.work.HiltWorker;
import androidx.navigation.NavDeepLinkBuilder;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.websarva.wings.android.zuboradiary.CustomApplication;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository;

import java.time.LocalDate;
import java.util.Objects;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class ReminderNotificationWorker extends Worker {

    private final String CHANNEL_ID;
    private final String CHANNEL_NAME;
    private final String CHANNEL_DESCRIPTION;
    private final String CONTENT_TITLE;
    private final String CONTENT_TEXT;
    private final int NOTIFY_ID;
    private final DiaryRepository diaryRepository;

    @AssistedInject
    public ReminderNotificationWorker(
            @Assisted Context context,
            @Assisted WorkerParameters workerParams,
            DiaryRepository diaryRepository) {
        super(context, workerParams);
        Objects.requireNonNull(diaryRepository);

        CHANNEL_ID = context.getString(R.string.reminder_notification_worker_channel_id);
        CHANNEL_NAME = context.getString(R.string.reminder_notification_worker_channel_name);
        CHANNEL_DESCRIPTION = context.getString(R.string.reminder_notification_worker_channel_description);
        CONTENT_TITLE = context.getString(R.string.reminder_notification_worker_content_title);
        CONTENT_TEXT = context.getString(R.string.reminder_notification_worker_content_text);
        NOTIFY_ID = 100;

        this.diaryRepository = diaryRepository;
        prepareNotificationManager();
    }

    @NonNull
    @Override
    public Result doWork() {
        CustomApplication application = (CustomApplication) getApplicationContext();
        boolean isAppInForeground = application.isAppInForeground();

        if (isAppInForeground) return Result.success();
        boolean hasWriteTodayDiary;
        try {
            hasWriteTodayDiary = existsSavedTodayDiary();
        } catch (Exception e) {
            Log.d("Exception", "本日付日記保存済み確認失敗", e);
            return Result.failure();
        }
        if (hasWriteTodayDiary) return Result.success();
        return showHeadsUpNotification();
    }

    private void prepareNotificationManager() {
        NotificationManager notificationManager =
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

    private boolean existsSavedTodayDiary() throws Exception{
        ListenableFuture<Boolean> listenableFuture = diaryRepository.existsDiary(LocalDate.now());
        Boolean result = listenableFuture.get();
        if (result == null) return false;
        Log.d("NotificationWorker", "hasWriteTodayDiary():" + result);
        return result;
    }

    private Result showHeadsUpNotification() {
        boolean isPermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isPermission =
                    ActivityCompat.checkSelfPermission(
                            getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                            == PackageManager.PERMISSION_GRANTED;
        } else {
            isPermission = true;
        }
        Log.d("NotificationWorker", "showHeadsUpNotification()_NotificationIsPermission:" + isPermission);

        if (!isPermission) return Result.failure();

        PendingIntent pendingIntent =
                new NavDeepLinkBuilder(getApplicationContext())
                        .setGraph(R.navigation.mobile_navigation)
                        .setDestination(R.id.navigation_diary_list_fragment)
                        .createPendingIntent();

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        Notification notification =
                builder.setSmallIcon(R.drawable.ic_notifications_24px)
                        .setContentTitle(CONTENT_TITLE)
                        .setContentText(CONTENT_TEXT)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(NOTIFY_ID, notification);
        return Result.success();
    }
}
