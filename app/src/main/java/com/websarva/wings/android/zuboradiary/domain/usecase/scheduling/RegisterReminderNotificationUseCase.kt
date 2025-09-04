package com.websarva.wings.android.zuboradiary.domain.usecase.scheduling

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.SchedulingRepository
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.ReminderNotificationRegistrationFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalTime

/**
 * 指定された時刻にリマインダー通知を登録するユースケース。
 *
 * @property schedulingRepository スケジューリング関連の操作を行うリポジトリ。
 */
internal class RegisterReminderNotificationUseCase(
    private val schedulingRepository: SchedulingRepository
) {

    private val logTag = createLogTag()
    private val logMsg = "リマインダー通知の登録_"

    /**
     * ユースケースを実行し、指定された時刻にリマインダー通知を登録する。
     *
     * @param notificationTime 通知を登録する時刻。
     * @return 登録処理が成功した場合は [UseCaseResult.Success] を返す。
     *   登録処理中にエラーが発生した場合は [UseCaseResult.Failure] を返す。
     */
    operator fun invoke(notificationTime: LocalTime): DefaultUseCaseResult<Unit> {
        Log.i(logTag, "${logMsg}開始 (通知時刻: $notificationTime)")

        try {
            schedulingRepository.registerReminderNotification(notificationTime)
        } catch (e: ReminderNotificationRegistrationFailureException) {
            Log.e(logTag, "${logMsg}失敗_登録処理エラー", e)
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
