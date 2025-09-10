package com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.UpdateThemeColorSettingUseCase

/**
 * [UpdateThemeColorSettingUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
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
     * @param setting 更新しようとした設定 [ThemeColorSetting] オブジェクト。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class UpdateFailure(
        setting: ThemeColorSetting,
        cause: Throwable
    ) : ThemeColorSettingUpdateException("テーマカラー設定 '${setting.themeColor}' の更新に失敗しました。", cause)
}
