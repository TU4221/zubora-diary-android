package com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception

import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UnknownException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadPasscodeLockSettingUseCase

/**
 * [LoadPasscodeLockSettingUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * 代替設定値を提供する。
 *
 * @property fallbackSetting 例外発生時に代わりに利用する設定。
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class PasscodeLockSettingLoadException (
    val fallbackSetting: PasscodeLockSetting,
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * パスコード設定の読み込みに失敗した場合にスローされる例外。
     *
     * @param fallbackSetting 例外発生時に代わりに利用する設定。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class LoadFailure(
        fallbackSetting: PasscodeLockSetting,
        cause: Throwable
    ) : PasscodeLockSettingLoadException(fallbackSetting, "パスコード設定の読込に失敗しました。", cause)

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param fallbackSetting 例外発生時に代わりに利用する設定。
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        fallbackSetting: PasscodeLockSetting,
        cause: Throwable
    ) : PasscodeLockSettingLoadException(
        fallbackSetting,
        "予期せぬエラーが発生しました。",
        cause
    ), UnknownException
}
