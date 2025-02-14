package com.websarva.wings.android.zuboradiary.data.worker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.websarva.wings.android.zuboradiary.CustomApplication
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.database.DiaryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class ReminderNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val diaryRepository: DiaryRepository
) : Worker(context, workerParams) {

    private val channelId: String = context.getString(R.string.reminder_notification_worker_channel_id)
    private val channelName: String = context.getString(R.string.reminder_notification_worker_channel_name)
    private val channelDescription: String = context.getString(R.string.reminder_notification_worker_channel_description)
    private val channelTitle: String = context.getString(R.string.reminder_notification_worker_content_title)
    private val channelText: String = context.getString(R.string.reminder_notification_worker_content_text)
    private val notifyId: Int = 100

    init {
        prepareNotificationManager()
    }

    private fun prepareNotificationManager() {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // MEMO:NotificationChannelはSdk26以降の機能
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
                    .apply{
                        description = channelDescription
                        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                        enableVibration(true)
                        setShowBadge(true)
                    }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun doWork(): Result {
        val application = applicationContext as CustomApplication
        if (application.isAppInForeground) return Result.success()

        val hasWriteTodayDiary: Boolean
        try {
            hasWriteTodayDiary = existsSavedTodayDiary()
        } catch (e: Exception) {
            Log.d("Exception", "本日付日記保存済み確認失敗", e)
            return Result.failure()
        }
        if (hasWriteTodayDiary) return Result.success()

        return showHeadsUpNotification()
    }

    @Throws(Exception::class)
    private fun existsSavedTodayDiary(): Boolean {
        val listenableFuture = diaryRepository.existsDiary(LocalDate.now())
        val result = listenableFuture.get() ?: return false
        Log.d("NotificationWorker", "hasWriteTodayDiary():$result")
        return result
    }

    private fun showHeadsUpNotification(): Result {
        val isPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        Log.d(
            "NotificationWorker",
            "showHeadsUpNotification()_NotificationIsPermission:$isPermission"
        )

        if (!isPermission) return Result.failure()

        val builder =
            NotificationCompat.Builder(applicationContext, channelId)

        val pendingIntent =
            NavDeepLinkBuilder(applicationContext)
                .setGraph(R.navigation.mobile_navigation)
                .setDestination(R.id.navigation_diary_list_fragment)
                .createPendingIntent()

        val notification =
            builder.setSmallIcon(R.drawable.ic_notifications_24px)
                .setContentTitle(channelTitle)
                .setContentText(channelText)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

        val notificationManagerCompat = NotificationManagerCompat.from(applicationContext)
        notificationManagerCompat.notify(notifyId, notification)
        return Result.success()
    }
}
