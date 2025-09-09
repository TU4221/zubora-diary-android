package com.websarva.wings.android.zuboradiary.domain.usecase.exception

import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.settings.DeleteAllDiariesUseCase

/**
 * [DeleteAllDiariesUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal sealed class AllDiariesDeleteException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 全ての日記データの削除に失敗した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class DeleteFailure(
        cause: Throwable
    ) : AllDiariesDeleteException(
        "全日記の削除に失敗しました。",
        cause
    )

    /**
     * 全ての永続的URI権限の解放に失敗した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class PersistableUriPermissionReleaseFailure(
        cause: Throwable
    ) : AllDiariesDeleteException(
        "全ての永続的URI権限の解放に失敗しました。",
        cause
    )
}
