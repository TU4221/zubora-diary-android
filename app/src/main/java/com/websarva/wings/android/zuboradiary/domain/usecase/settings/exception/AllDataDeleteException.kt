package com.websarva.wings.android.zuboradiary.domain.usecase.settings.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.DeleteAllDataUseCase

/**
 * [DeleteAllDataUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal sealed class AllDataDeleteException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 全ての日記関係データの削除に失敗した場合の例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class DiariesDeleteFailure(
        cause: Throwable
    ) : AllDataDeleteException(
        "全日記関係データの削除に失敗しました。",
        cause
    )

    /**
     * 全ての画像ファイルの削除に失敗した場合の例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class ImageFileDeleteFailure(
        cause: Throwable
    ) : AllDataDeleteException(
        "全ての画像ファイルの削除に失敗しました。",
        cause
    )

    /**
     * 全ての設定の初期化に失敗した場合の例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class SettingsInitializationFailure(
        cause: Throwable
    ) : AllDataDeleteException(
        "全ての設定の初期化に失敗しました。",
        cause
    )
}
