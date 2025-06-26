package com.websarva.wings.android.zuboradiary.domain.exception.reminder

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class CancelReminderNotificationFailedException (
    cause: Throwable
) : DomainException("リマインダー通知のキャンセルに失敗しました。", cause)
