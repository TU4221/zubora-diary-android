package com.websarva.wings.android.zuboradiary.domain.usecase.scheduling

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.domain.repository.SchedulingRepository
import com.websarva.wings.android.zuboradiary.domain.usecase.scheduling.exception.ReminderNotificationRegisterException
import com.websarva.wings.android.zuboradiary.domain.repository.exception.SchedulingException
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
     * @return 処理に成功した場合は [UseCaseResult.Success] に `Unit` を格納して返す。
     *   失敗した場合は [UseCaseResult.Failure] に [ReminderNotificationRegisterException] を格納して返す。
     */
    operator fun invoke(notificationTime: LocalTime): UseCaseResult<Unit, ReminderNotificationRegisterException> {
        Log.i(logTag, "${logMsg}開始 (通知時刻: $notificationTime)")

        try {
            schedulingRepository.registerReminderNotification(notificationTime)
        } catch (e: SchedulingException) {
            Log.e(logTag, "${logMsg}失敗_登録処理エラー", e)
            return UseCaseResult.Failure(
                ReminderNotificationRegisterException.RegisterFailure(e)
            )
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
