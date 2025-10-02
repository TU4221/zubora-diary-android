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
        createExceptionBaseMessage(setting),
        cause
    )

    /**
     * ストレージ容量不足により、リマインダー通知設定の更新に失敗した場合にスローされる例外。
     *
     * @param setting 更新しようとした設定 [ReminderNotificationSetting] 。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class InsufficientStorage(
        setting: ReminderNotificationSetting,
        cause: Throwable
    ) : ReminderNotificationSettingUpdateException(
        "ストレージ容量不足により、" + createExceptionBaseMessage(setting),
        cause
    )

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        cause: Throwable
    ) : ReminderNotificationSettingUpdateException(
        "予期せぬエラーが発生しました。",
        cause
    )

    companion object {
        /**
         * 指定されたリマインダー通知設定に基づき、更新失敗エラーメッセージの共通部分を生成する。
         *
         * 具体的なエラー原因は含めず、どの設定の更新に失敗したかを識別するための基本メッセージとなる。
         *
         * @param setting 更新に失敗した [ReminderNotificationSetting]。
         * @return 更新対象の設定値を含む、基本的なエラーメッセージ文字列。
         *         例: "リマインダー通知設定 '有効 ''10:00''' の更新に失敗しました。" または "リマインダー通知設定 '無効' の更新に失敗しました。"
         */
        private fun createExceptionBaseMessage(setting: ReminderNotificationSetting): String {
            val statusMessage = if (setting is ReminderNotificationSetting.Enabled) {
                "有効 '${setting.notificationTime}'"
            } else {
                "無効"
            }
            return "リマインダー通知設定 '$statusMessage' の更新に失敗しました。"
        }
    }
}
