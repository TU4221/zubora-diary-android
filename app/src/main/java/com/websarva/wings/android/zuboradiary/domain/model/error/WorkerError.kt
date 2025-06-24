package com.websarva.wings.android.zuboradiary.domain.model.error

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseError

internal sealed class WorkerError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class RegisterReminderNotification(
        cause: Throwable? = null
    ) : WorkerError(
        "リマインダ通知の登録に失敗しました。",
        cause
    )

    class CancelReminderNotification(
        cause: Throwable? = null
    ) : WorkerError(
        "リマインダ通知のキャンセルに失敗しました。",
        cause
    )
}
