package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryItemTitleSelectionHistoryItemUseCase

/**
 * [DeleteDiaryItemTitleSelectionHistoryItemUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class DiaryItemTitleSelectionHistoryItemDeleteException (
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 日記項目のタイトル選択履歴から特定の項目の削除に失敗した場合にスローされる例外。
     *
     * @param title 削除しようとした日記項目のタイトル。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class DeleteFailure (
        title: String,
        cause: Throwable
    ) : DiaryItemTitleSelectionHistoryItemDeleteException(
        "日記項目タイトル選択履歴の '$title' の削除に失敗しました。",
        cause
    )
}
