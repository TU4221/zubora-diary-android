package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.RefreshWordSearchResultListUseCase

/**
 * [RefreshWordSearchResultListUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class WordSearchResultListRefreshException (
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 既存のワード検索結果リストの再読み込みに失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class RefreshFailure(
        cause: Throwable
    ) : WordSearchResultListRefreshException("既存のワード検索結果リストの再読込に失敗しました。", cause)

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        cause: Throwable
    ) : WordSearchResultListRefreshException(
        "予期せぬエラーが発生しました。",
        cause
    )
}
