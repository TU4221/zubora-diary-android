package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadAdditionWordSearchResultListUseCase

/**
 * [LoadAdditionWordSearchResultListUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class WordSearchResultListAdditionLoadException (
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * ワード検索結果リストの追加読み込みに失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class LoadFailure(
        cause: Throwable
    ) : WordSearchResultListAdditionLoadException("ワード検索結果リストの追加読込に失敗しました。", cause)

    /**
     * ワード検索結果リストのフッターの更新に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class FooterUpdateFailure(
        cause: Throwable
    ) : WordSearchResultListAdditionLoadException("ワード検索結果リストのフッターの更新に失敗しました。", cause)
}
