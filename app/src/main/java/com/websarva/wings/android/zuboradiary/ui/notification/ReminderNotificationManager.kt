package com.websarva.wings.android.zuboradiary.ui.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.websarva.wings.android.zuboradiary.R

/**
 * リマインダー通知の表示を管理するクラス。
 *
 * 通知のタイトル、本文、アイコン、タップ時の動作などを設定し、
 * [NotificationManagerCompat] を介してユーザーに通知を表示する。
 *
 * このクラスは、通知チャネルIDや通知IDといった定数を利用して、一貫性のある通知体験を提供する。
 *
 * @param context リソースへのアクセスやPendingIntentの作成に使用される。
 * @param notificationManager システムの通知サービスと連携し、通知を発行・管理する。
 */
internal class ReminderNotificationManager(
    context: Context,
    private val notificationManager: NotificationManagerCompat
) {

    companion object{
        /**
         * リマインダー通知用チャネルID
         * */
        const val CHANNEL_ID = "reminder_notification"
    }

    /**
     * 表示するリマインダー通知オブジェクト。
     */
    private val notification: Notification

    init {
        notification = createNotification(context)
    }

    /**
     * リマインダー通知オブジェクトを構築する。
     *
     * 通知のアイコン、タイトル、本文、優先度、デフォルト設定、タップ時の [PendingIntent] 、自動キャンセルを設定する。
     *
     * @return 構築された [Notification] オブジェクト。
     */
    private fun createNotification(context: Context): Notification {
        val pendingIntent = createPendingIntent(context)
        return NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_24px)
            .setContentTitle(context.getString(R.string.reminder_notification_worker_content_title))
            .setContentText(context.getString(R.string.reminder_notification_worker_content_text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

    /**
     * 通知がタップされたときに起動する [PendingIntent] を作成する。
     *
     * このPendingIntentは、日記一覧画面 ([R.id.navigation_diary_list_fragment]) への
     * ディープリンクとして機能する。
     *
     * @return 作成された [PendingIntent]。
     */
    private fun createPendingIntent(context: Context): PendingIntent {
        return NavDeepLinkBuilder(context)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.navigation_diary_list_fragment)
            .createPendingIntent()
    }

    /**
     * 構築済みのリマインダー通知をユーザーに表示する。
     *
     * このメソッドを呼び出すには、`android.permission.POST_NOTIFICATIONS` 権限が許可されている必要がある。
     * 権限チェックは、このメソッドの呼び出し元で行われることを想定している。
     *
     * @throws SecurityException 必要な権限がない場合。
     */
    @RequiresPermission("android.permission.POST_NOTIFICATIONS")
    fun show() {
        notificationManager.notify(100, notification)
    }
}
