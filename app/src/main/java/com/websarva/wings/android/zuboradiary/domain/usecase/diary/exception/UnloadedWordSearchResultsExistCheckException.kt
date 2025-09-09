package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CheckUnloadedWordSearchResultsExistUseCase

/**
 * [CheckUnloadedWordSearchResultsExistUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class UnloadedWordSearchResultsExistCheckException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 読み込まれていないワード検索結果が存在するかどうかを確認するのに失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class CheckFailure(
        cause: Throwable
    ) : UnloadedWordSearchResultsExistCheckException(
        "読み込まれていないワード検索結果が存在するかどうかを確認するのに失敗しました。",
        cause
    )
}
