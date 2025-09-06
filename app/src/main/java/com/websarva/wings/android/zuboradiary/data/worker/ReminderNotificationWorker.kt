package com.websarva.wings.android.zuboradiary.data.worker

import android.annotation.SuppressLint
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.websarva.wings.android.zuboradiary.data.common.AppForegroundStateProvider
import com.websarva.wings.android.zuboradiary.data.common.PermissionChecker
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
 * @property permissionChecker 通知パーミッションなど、必要な権限が付与されているかを確認する機能を提供。
 * @property diaryRepository 日記データへのアクセスを提供するリポジトリ。
 */
@HiltWorker
internal class ReminderNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val reminderNotifier: ReminderNotifier,
    private val appForegroundStateProvider: AppForegroundStateProvider,
    private val permissionChecker: PermissionChecker,
    private val diaryRepository: DiaryRepositoryImpl
) : CoroutineWorker(context, workerParams) {

    /**
     * ワーカーのメイン処理を実行する。
     *
     * アプリがフォアグラウンドでない、かつ当日の日記が未保存の場合にリマインダー通知を表示する。
     *
     * @return ワーカーの実行結果。
     */
    override suspend fun doWork(): Result {
        if (appForegroundStateProvider.isAppInForeground) return Result.success()
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
     * @return 通知の表示に成功した場合は [Result.success]、パーミッションがない場合は [Result.failure]。
     */
    @SuppressLint("MissingPermission")
    private fun showHeadsUpNotification(): Result {
        if (!permissionChecker.isPostNotificationsGranted) return Result.failure()

        reminderNotifier.show()
        return Result.success()
    }
}
