package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryItemTitle
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseUnknownException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryItemTitleSelectionHistoryUseCase

/**
 * [DeleteDiaryItemTitleSelectionHistoryUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class DiaryItemTitleSelectionHistoryDeleteException (
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
        title: DiaryItemTitle,
        cause: Throwable
    ) : DiaryItemTitleSelectionHistoryDeleteException(
        "日記項目タイトル選択履歴の '$title' の削除に失敗しました。",
        cause
    )

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        cause: Throwable
    ) : DiaryItemTitleSelectionHistoryDeleteException(
        "予期せぬエラーが発生しました。",
        cause
    ), UseCaseUnknownException
}
