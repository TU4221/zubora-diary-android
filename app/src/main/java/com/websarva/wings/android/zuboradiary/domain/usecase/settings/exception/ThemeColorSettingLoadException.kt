package com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception

import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.LoadThemeColorSettingUseCase

/**
 * [LoadThemeColorSettingUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * 代替設定値を提供する。
 *
 * @property fallbackSetting 例外発生時に代わりに利用する設定。
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class ThemeColorSettingLoadException (
    val fallbackSetting: ThemeColorSetting,
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * テーマカラー設定の読み込みに失敗した場合にスローされる例外。
     *
     * @param fallbackSetting 例外発生時に代わりに利用する設定。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class LoadFailure(
        fallbackSetting: ThemeColorSetting,
        cause: Throwable
    ) : ThemeColorSettingLoadException(fallbackSetting, "テーマカラー設定の読込に失敗しました。", cause)
}
