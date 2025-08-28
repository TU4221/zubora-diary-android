package com.websarva.wings.android.zuboradiary.domain.usecase.scheduling

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseResult
import com.websarva.wings.android.zuboradiary.data.repository.SchedulingRepository
import com.websarva.wings.android.zuboradiary.domain.exception.reminder.ReminderNotificationRegistrationFailureException
import com.websarva.wings.android.zuboradiary.domain.usecase.DefaultUseCaseResult
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import java.time.LocalTime

internal class RegisterReminderNotificationUseCase(
    private val schedulingRepository: SchedulingRepository
) {

    private val logTag = createLogTag()

    operator fun invoke(notificationTime: LocalTime): DefaultUseCaseResult<Unit> {
        val logMsg = "リマインダー通知の登録_"
        Log.i(logTag, "${logMsg}開始")

        try {
            schedulingRepository.registerReminderNotificationWorker(notificationTime)
        } catch (e: ReminderNotificationRegistrationFailureException) {
            Log.e(logTag, "${logMsg}失敗", e)
            return UseCaseResult.Failure(e)
        }

        Log.i(logTag, "${logMsg}完了")
        return UseCaseResult.Success(Unit)
    }
}
