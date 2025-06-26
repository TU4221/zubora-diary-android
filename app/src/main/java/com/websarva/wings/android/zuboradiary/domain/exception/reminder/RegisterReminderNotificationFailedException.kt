package com.websarva.wings.android.zuboradiary.domain.exception.reminder

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class RegisterReminderNotificationFailedException (
    cause: Throwable
) : DomainException("リマインダー通知の登録に失敗しました。", cause)
