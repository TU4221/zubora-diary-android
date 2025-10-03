package com.websarva.wings.android.zuboradiary.data.worker

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.websarva.wings.android.zuboradiary.data.common.AppForegroundStateProvider
import com.websarva.wings.android.zuboradiary.data.repository.DiaryRepositoryImpl
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

/**
 * 日記の記入を促すリマインダー通知をスケジュール実行するためのワーカー。
 *
 * このワーカーは、ユーザーが設定した時刻に起動し、以下の条件を満たす場合に通知を表示する。
 * - アプリがフォアグラウンドで実行されていない。
 * - 当日の日記がまだ保存されていない。
 * - 通知のパーミッションが付与されている
 *
 * @param context アプリケーションコンテキスト。
 * @param workerParams ワーカーのパラメータ。
 * @property reminderNotifier リマインダー通知を実際に表示する機能を提供。
 * @property appForegroundStateProvider アプリケーションがフォアグラウンドで実行されているかどうかの状態を提供。
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
@HiltWorker
internal class ReminderNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val reminderNotifier: ReminderNotifier,
    private val appForegroundStateProvider: AppForegroundStateProvider,
    private val diaryRepository: DiaryRepositoryImpl
) : CoroutineWorker(context, workerParams) {

    /**
     * ワーカーのメイン処理を実行する。
     *
     * アプリがフォアグラウンドでない、かつ当日の日記が未保存の場合にリマインダー通知を表示する。
     *
     * @return ワーカーの実行結果。
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        return try {
            if (appForegroundStateProvider.isAppInForeground) return Result.success()
            if (diaryRepository.existsDiary(LocalDate.now())) return Result.success()
            reminderNotifier.show()
            Result.success()
        } catch (_: Exception) {
            Result.failure()
        }
    }
}
