package com.websarva.wings.android.zuboradiary.domain.exception.settings

import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveReminderNotificationSettingUseCase
import java.time.LocalTime

/**
 * [SaveReminderNotificationSettingUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class ReminderNotificationSettingUpdateException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {
    /**
     * リマインダー通知設定のバックアップの取得(設定の読込)に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class BackupFailure(
        cause: Throwable
    ) : ReminderNotificationSettingUpdateException(
        "リマインダー通知設定のバックアップの取得(設定の読込)に失敗しました。",
        cause
    )

    /**
     * リマインダー通知設定の更新に失敗した場合にスローされる例外。
     *
     * @param isEnabled 更新しようとしたリマインダー通知設定値。
     * @param time リマインダー通知時間。無効の場合は `null` 。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class UpdateFailure(
        isEnabled: Boolean,
        time: LocalTime? = null,
        cause: Throwable
    ) : ReminderNotificationSettingUpdateException(
        "リマインダー通知設定 '${
            if (isEnabled) {
                "有効 '${time ?: ""}'"
            } else {
                "無効"
            }
        }' の更新に失敗しました。"
        , cause
    )

    /**
     * リマインダー通知設定のスケジュール登録に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class SchedulingRegisterFailure(
        cause: Throwable
    ) : ReminderNotificationSettingUpdateException("リマインダー通知のスケジュール登録に失敗しました。", cause)

    /**
     * リマインダー通知設定のスケジュール解除に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class SchedulingCancelFailure(
        cause: Throwable
    ) : ReminderNotificationSettingUpdateException("リマインダー通知のスケジュール解除に失敗しました。", cause)

    /**
     * リマインダー通知設定のスケジュール操作に失敗した後のロールバックに失敗した場合にスローされる例外。
     *
     * @param isEnabled ロールバックしようとしたリマインダー通知設定値。
     * @param time リマインダー通知時間。無効の場合は `null` 。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class RollbackFailure(
        isEnabled: Boolean,
        time: LocalTime? = null,
        cause: Throwable
    ) : ReminderNotificationSettingUpdateException(
        "リマインダー通知設定 '${
            if (isEnabled) {
                "有効 '${time ?: ""}'"
            } else {
                "無効"
            }
        }' のロールバックに失敗しました。"
        , cause
    )
}
