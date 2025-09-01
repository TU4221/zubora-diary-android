package com.websarva.wings.android.zuboradiary.domain.exception.reminder

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

/**
 * リマインダー通知の登録処理中に予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class ReminderNotificationRegistrationFailureException (
    cause: Throwable
) : DomainException("リマインダー通知の登録に失敗しました。", cause)
