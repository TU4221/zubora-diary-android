package com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.UpdatePasscodeLockSettingUseCase

/**
 * [UpdatePasscodeLockSettingUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class PassCodeSettingUpdateException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * パスコード設定の更新に失敗した場合にスローされる例外。
     *
     * @param isEnabled 更新しようとしたパスコード設定値。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class UpdateFailure(
        isEnabled: Boolean,
        cause: Throwable
    ) : PassCodeSettingUpdateException(
        "パスコード設定 '${
            if (isEnabled) {
                "有効"
            } else {
                "無効"
            }
        }' の更新に失敗しました。"
        , cause
    )
}
