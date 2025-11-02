package com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception

import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseUnknownException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
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
     * @param setting 更新しようとした設定 [ThemeColorSetting] 。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class UpdateFailure(
        setting: ThemeColorSetting,
        cause: Throwable
    ) : ThemeColorSettingUpdateException(
        createExceptionBaseMessage(setting),
        cause
    )

    /**
     * ストレージ容量不足により、テーマカラー設定の更新に失敗した場合にスローされる例外。
     *
     * @param setting 更新しようとした設定 [ThemeColorSetting] 。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class InsufficientStorage(
        setting: ThemeColorSetting,
        cause: Throwable
    ) : ThemeColorSettingUpdateException(
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
    ) : ThemeColorSettingUpdateException(
        "予期せぬエラーが発生しました。",
        cause
    ), UseCaseUnknownException

    companion object {
        /**
         * 指定されたテーマカラー設定に基づき、更新失敗エラーメッセージの共通部分を生成する。
         *
         * 具体的なエラー原因は含めず、どの設定の更新に失敗したかを識別するための基本メッセージとなる。
         *
         * @param setting 更新に失敗した [ThemeColorSetting]。
         * @return 更新対象の設定値を含む、基本的なエラーメッセージ文字列。
         *         例: "テーマカラー設定 'ブルー' の更新に失敗しました。"
         */
        private fun createExceptionBaseMessage(setting: ThemeColorSetting): String {
            return "テーマカラー設定 '${setting.themeColor}' の更新に失敗しました。"
        }
    }
}
