package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException
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
     * 日記リストのフッターの更新に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class FooterUpdateFailure(
        cause: Throwable
    ) : WordSearchResultListRefreshException("ワード検索結果リストのフッターの更新に失敗しました。", cause)
}
