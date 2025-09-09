package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.UpdateWordSearchResultListFooterUseCase

/**
 * [UpdateWordSearchResultListFooterUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class WordSearchListFooterUpdateFailureException (
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    class UpdateFailure (
        cause: Throwable
    ) : WordSearchListFooterUpdateFailureException(
        "ワード検索結果リストのフッターを更新に失敗しました。",
        cause
    )
}
