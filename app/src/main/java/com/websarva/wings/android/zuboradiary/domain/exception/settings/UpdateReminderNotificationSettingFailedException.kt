package com.websarva.wings.android.zuboradiary.domain.exception.settings

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import java.time.LocalTime

internal class UpdateReminderNotificationSettingFailedException(
    isEnabled: Boolean,
    time: LocalTime? = null,
    cause: Throwable
) : DomainException(
    "リマインダー通知設定 '${
        if (isEnabled) {
            "有効 '${time ?: ""}'"
        } else {
            "無効"
        }
    }' の更新に失敗しました。"
    , cause
)
