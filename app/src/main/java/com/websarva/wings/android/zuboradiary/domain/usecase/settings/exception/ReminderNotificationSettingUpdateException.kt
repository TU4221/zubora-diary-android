package com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception

import com.websarva.wings.android.zuboradiary.domain.model.settings.ReminderNotificationSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.UpdateReminderNotificationSettingUseCase

/**
 * [UpdateReminderNotificationSettingUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class ReminderNotificationSettingUpdateException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {
    /**
     * リマインダー通知設定の更新に失敗した場合にスローされる例外。
     *
     * @param setting 更新しようとした設定 [ReminderNotificationSetting] 。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class SettingUpdateFailure(
        setting: ReminderNotificationSetting,
        cause: Throwable
    ) : ReminderNotificationSettingUpdateException(
        "リマインダー通知設定 '${
            if (setting is ReminderNotificationSetting.Enabled) {
                "有効 '${setting.notificationTime}'"
            } else {
                "無効"
            }
        }' の更新に失敗しました。"
        , cause
    )

    /**
     * リマインダー通知設定のスケジュール更新に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class SchedulingUpdateFailure(
        setting: ReminderNotificationSetting,
        cause: Throwable
    ) : ReminderNotificationSettingUpdateException(
        "リマインダー通知のスケジュール" +
                (if (setting.isEnabled) "登録" else "解除") +
                "に失敗しました。",
        cause
    )
}
