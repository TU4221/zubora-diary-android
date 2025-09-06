package com.websarva.wings.android.zuboradiary.data.worker

import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationManagerCompat

/**
 * リマインダー通知を表示する機能を提供するインターフェース。
 *
 * 通知を表示するWorkerは、このインターフェースを通じて通知機能を利用することで、具体的な通知の実装詳細から分離する。
 *
 * @see show メソッドは通知を表示するために `android.permission.POST_NOTIFICATIONS` 権限を必要とする。
 */
interface ReminderNotifier {

    /**
     * リマインダー通知をユーザーに表示する。
     *
     * このメソッドは [NotificationManagerCompat.notify] が処理されることを前提とする。
     * このメソッドはを呼び出すには、`android.permission.POST_NOTIFICATIONS` 権限が許可されている必要がある。
     *
     * @throws SecurityException 必要な権限がない場合。
     */
    @RequiresPermission(value = "android.permission.POST_NOTIFICATIONS")
    fun show()
}
