package com.websarva.wings.android.zuboradiary.data.worker

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.websarva.wings.android.zuboradiary.ZuboraDiaryApplication
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepositoryImpl
import com.websarva.wings.android.zuboradiary.ui.utils.isPostNotificationsGranted
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

/**
 * 日記の記入を促すリマインダー通知をスケジュール実行するためのワーカー。
 *
 * このワーカーは、ユーザーが設定した時刻に起動し、以下の条件を満たす場合に通知を表示する。
 * - アプリがフォアグラウンドで実行されていない。
 * - 当日の日記がまだ保存されていない。
 * - 通知のパーミッションが付与されている (Android 13以降)。
 *
 * 通知をタップすると、日記一覧画面 ([R.id.navigation_diary_list_fragment]) に遷移する。
 *
 * @param context アプリケーションコンテキスト。
 * @param workerParams ワーカーのパラメータ。
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
@HiltWorker
internal class ReminderNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val diaryRepository: DiaryRepositoryImpl
) : CoroutineWorker(context, workerParams) {

    private val logTag = createLogTag()

    private val channelId: String = context.getString(R.string.reminder_notification_worker_channel_id)
    private val channelName: String = context.getString(R.string.reminder_notification_worker_channel_name)
    private val channelDescription: String = context.getString(R.string.reminder_notification_worker_channel_description)
    private val channelTitle: String = context.getString(R.string.reminder_notification_worker_content_title)
    private val channelText: String = context.getString(R.string.reminder_notification_worker_content_text)
    private val notifyId: Int = 100

    init {
        prepareNotificationManager()
    }

    /**
     * 通知チャネルを準備する。
     * Android O (API 26) 以降では、通知を表示する前に通知チャネルを作成する必要がある。
     * このメソッドは、リマインダー通知用のチャネルを作成し、設定する。
     */
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

    /**
     * ワーカーのメイン処理を実行する。
     *
     * アプリがフォアグラウンドでない、かつ当日の日記が未保存の場合にリマインダー通知を表示する。
     *
     * @return ワーカーの実行結果。
     */
    override suspend fun doWork(): Result {
        val application = applicationContext as ZuboraDiaryApplication
        if (application.isAppInForeground) return Result.success()
        if (existsSavedTodayDiary()) return Result.success()

        return showHeadsUpNotification()
    }

    /**
     * 当日の日記が既に保存されているかどうかを確認する。
     *
     * @return 当日の日記が存在する場合はtrue、存在しないまたは確認に失敗した場合はfalse。
     */
    private suspend fun existsSavedTodayDiary(): Boolean {
        return try {
            diaryRepository.existsDiary(LocalDate.now())
        } catch (e: Exception) {
            false
        }
    }

    /**
     * HeadsUp通知を表示する。
     *
     * 通知をタップすると、日記一覧画面に遷移するPendingIntentが設定される。
     *
     * @return 通知の表示に成功した場合は [Result.success]、パーミッションがない場合は [Result.failure]。
     */
    @SuppressLint("MissingPermission")
    private fun showHeadsUpNotification(): Result {
        val isPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                applicationContext.isPostNotificationsGranted()
            } else {
                true
            }
        Log.d(logTag, "showHeadsUpNotification()_NotificationIsPermission = $isPermission")

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
