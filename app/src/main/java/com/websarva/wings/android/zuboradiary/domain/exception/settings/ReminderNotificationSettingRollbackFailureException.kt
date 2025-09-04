package com.websarva.wings.android.zuboradiary.domain.exception.settings

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import java.time.LocalTime

/**
 * リマインダー通知設定のロールバック処理中に予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class ReminderNotificationSettingRollbackFailureException(
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
    }' のロールバックに失敗しました。"
    , cause
)
