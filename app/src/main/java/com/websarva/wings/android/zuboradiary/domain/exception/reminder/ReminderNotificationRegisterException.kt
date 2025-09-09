package com.websarva.wings.android.zuboradiary.domain.exception.reminder

import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.scheduling.RegisterReminderNotificationUseCase

/**
 * [RegisterReminderNotificationUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class ReminderNotificationRegisterException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * リマインダー通知の登録に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class RegisterFailure(
        cause: Throwable
    ) : ReminderNotificationRegisterException("リマインダー通知の登録に失敗しました。", cause)
}
