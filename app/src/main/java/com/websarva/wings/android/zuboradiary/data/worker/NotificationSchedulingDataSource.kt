package com.websarva.wings.android.zuboradiary.data.worker

import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * [WorkManager] を使用して通知のスケジュール登録とキャンセルを行うデータソースクラス。
 *
 * [ReminderNotificationWorker] の定期実行を管理する。
 *
 * @property workManager WorkManagerのインスタンス。
 */
internal class NotificationSchedulingDataSource(private val workManager: WorkManager) {

    private val reminderNotificationWorkTag = "ReminderNotification"
    private val reminderNotificationUniqueWorkName = reminderNotificationWorkTag

    private val logTag = createLogTag()

    /**
     * リマインダー通知ワーカー ([ReminderNotificationWorker]) を指定された時刻に毎日実行するようにスケジュール登録する。
     *
     * 既存の同じ名前のワーカーが存在する場合は、キャンセルしてから新しいワーカーを登録する
     *
     * @param settingTime 通知を毎日表示する時刻。
     * @throws WorkProfileAccessFailureException WorkManagerの操作に失敗した場合。
     */
    fun registerReminderNotificationWorker(settingTime: LocalTime) {
        cancelReminderNotificationWorker()

        val nowTime = LocalTime.now()
        val initialDelaySeconds = calculationBetweenSeconds(nowTime, settingTime)
        Log.d(
            logTag,
            "registerReminderNotificationWorker()_initialDelaySeconds = $initialDelaySeconds"
        )
        val request =
            PeriodicWorkRequest
                .Builder(ReminderNotificationWorker::class.java, 1, TimeUnit.DAYS)
                .addTag(reminderNotificationWorkTag)
                .setInitialDelay(initialDelaySeconds, TimeUnit.SECONDS)
                .build()
        executeWorkOperation {
            workManager.enqueueUniquePeriodicWork(
                reminderNotificationUniqueWorkName,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                request
            )
        }
    }

    /**
     * 指定された開始時刻と終了時刻の間の秒数を計算する。
     *
     * 終了時刻が開始時刻より前の場合は、翌日の同じ時刻までの秒数を計算する
     * (例: 開始23:00, 終了01:00 の場合、2時間分の秒数を返す)。
     *
     * @param startTime 開始時刻。
     * @param endTime 終了時刻。
     * @return 開始時刻から終了時刻までの秒数。
     */
    private fun calculationBetweenSeconds(startTime: LocalTime, endTime: LocalTime): Long {
        val betweenSeconds = ChronoUnit.SECONDS.between(startTime, endTime)
        if (betweenSeconds < 0) {
            return 60 /*seconds*/ * 60 /*minutes*/ * 24 /*hours*/ + betweenSeconds
        }
        return betweenSeconds
    }

    /**
     * 現在スケジュールされているリマインダー通知ワーカーを全てキャンセルする。
     *
     * @throws WorkProfileAccessFailureException WorkManagerの操作に失敗した場合。
     */
    fun cancelReminderNotificationWorker() {
        executeWorkOperation {
            workManager.apply {
                cancelAllWorkByTag(reminderNotificationWorkTag)
                cancelUniqueWork(reminderNotificationUniqueWorkName)
            }
        }
    }

    /**
     * WorkManagerの操作を安全に実行するための共通ヘルパー関数。
     *
     * 指定された [operation] を実行し、[IllegalStateException] が発生した場合は
     * [WorkProfileAccessFailureException] としてラップして再スローする。
     * これは、WorkManagerが未初期化、または内部状態が不正な場合に発生する。
     *
     * @param operation WorkManagerに対する操作を行う関数。
     * @throws WorkProfileAccessFailureException WorkManagerの操作に失敗した場合。
     */
    private fun executeWorkOperation(
        operation: () -> Unit
    ) {
        try {
            operation()
        } catch (e: IllegalStateException) {
            // WorkManagerが未初期化、または内部状態が不正な場合に発生しうるためキャッチ
            throw WorkProfileAccessFailureException(e)
        }
    }
}
