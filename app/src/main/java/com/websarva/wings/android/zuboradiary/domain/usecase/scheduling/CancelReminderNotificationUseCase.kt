package com.websarva.wings.android.zuboradiary.domain.usecase.scheduling

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.SchedulingRepository
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.ReminderNotificationCancellationFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag

/**
 * 設定されているリマインダー通知を解除するユースケース。
 *
 * @property schedulingRepository スケジューリング関連の操作を行うリポジトリ。
 */
internal class CancelReminderNotificationUseCase(
    private val schedulingRepository: SchedulingRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "リマインダー通知の解除_"

    /**
     * ユースケースを実行し、リマインダー通知を解除する。
     *
     * @return 解除処理が成功した場合は [UseCaseResult.Success] を返す。
     *   解除処理中にエラーが発生した場合は [UseCaseResult.Failure] を返す。
     */
    operator fun invoke(): DefaultUseCaseResult<Unit> {
        Log.i(logTag, "${logMsg}開始")

        try {
            schedulingRepository.cancelReminderNotification()
        } catch (e: ReminderNotificationCancellationFailureException) {
            Log.e(logTag, "${logMsg}失敗_解除処理エラー", e)
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
