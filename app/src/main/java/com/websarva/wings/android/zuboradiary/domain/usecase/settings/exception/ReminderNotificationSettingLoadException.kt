package com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadReminderNotificationSettingUseCase

/**
 * [LoadReminderNotificationSettingUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class ReminderNotificationSettingLoadException (
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * リマインダー通知設定の読み込みに失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class LoadFailure(
        cause: Throwable
    ) : ReminderNotificationSettingLoadException("リマインダー通知設定の読込に失敗しました。", cause)
}
