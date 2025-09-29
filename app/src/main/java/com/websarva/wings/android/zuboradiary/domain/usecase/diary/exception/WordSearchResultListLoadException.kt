package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadWordSearchResultListUseCase

/**
 * [LoadWordSearchResultListUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class WordSearchResultListLoadException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 検索ワードに一致する日記のリストの読み込みに失敗した場合にスローされる例外。
     *
     * @param searchWord 検索に使用されたキーワード。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class LoadFailure(
        searchWord: String,
        cause: Throwable
    ) : WordSearchResultListLoadException("'$searchWord' の検索結果の読込に失敗しました。", cause)

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        cause: Throwable
    ) : WordSearchResultListLoadException(
        "予期せぬエラーが発生しました。",
        cause
    )
}
