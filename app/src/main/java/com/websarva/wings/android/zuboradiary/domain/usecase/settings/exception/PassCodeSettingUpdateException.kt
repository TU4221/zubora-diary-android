package com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception

import com.websarva.wings.android.zuboradiary.domain.model.settings.PasscodeLockSetting
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
     * @param setting 更新しようとした設定 [PasscodeLockSetting] オブジェクト]。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class UpdateFailure(
        setting: PasscodeLockSetting,
        cause: Throwable
    ) : PassCodeSettingUpdateException(
        "パスコード設定 '${
            if (setting.isEnabled) {
                "有効"
            } else {
                "無効"
            }
        }' の更新に失敗しました。"
        , cause
    )
}
