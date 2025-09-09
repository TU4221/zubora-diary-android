package com.websarva.wings.android.zuboradiary.domain.usecase.scheduling.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.scheduling.CancelReminderNotificationUseCase

/**
 * [CancelReminderNotificationUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class ReminderNotificationCancelException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * リマインダー通知のキャンセルに失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class CancelFailure(
        cause: Throwable
    ) : ReminderNotificationCancelException("リマインダー通知のキャンセルに失敗しました。", cause)
}
