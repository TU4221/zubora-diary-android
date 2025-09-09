package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CountWordSearchResultsUseCase

/**
 * [CountWordSearchResultsUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class WordSearchResultCountException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 検索ワードに一致する日記の総数を取得に失敗した場合にスローされる例外。
     *
     * @param searchWord 検索に使用されたキーワード。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class CountFailure(
        searchWord: String,
        cause: Throwable
    ) : WordSearchResultCountException(
        "検索ワード '$searchWord' に一致する日記の総数の取得に失敗しました。",
        cause
    )
}
