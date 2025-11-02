package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseUnknownException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.UpdateWordSearchResultListFooterUseCase

/**
 * [UpdateWordSearchResultListFooterUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class WordSearchListFooterUpdateException (
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    class UpdateFailure (
        cause: Throwable
    ) : WordSearchListFooterUpdateException(
        "ワード検索結果リストのフッターを更新に失敗しました。",
        cause
    )

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        cause: Throwable
    ) : WordSearchListFooterUpdateException(
        "予期せぬエラーが発生しました。",
        cause
    ), UseCaseUnknownException
}
