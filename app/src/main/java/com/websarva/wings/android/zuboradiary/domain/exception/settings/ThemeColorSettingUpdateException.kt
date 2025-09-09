package com.websarva.wings.android.zuboradiary.domain.exception.settings

import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.SaveThemeColorSettingUseCase

/**
 * [SaveThemeColorSettingUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class ThemeColorSettingUpdateException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * テーマカラー設定の更新に失敗した場合にスローされる例外。
     *
     * @param themeColor 更新しようとしたテーマカラー。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class UpdateFailure(
        themeColor: ThemeColor,
        cause: Throwable
    ) : ThemeColorSettingUpdateException("テーマカラー設定 '$themeColor' の更新に失敗しました。", cause)
}
