package com.websarva.wings.android.zuboradiary.domain.model.error

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseError

internal sealed class DiaryItemTitleSelectionHistoryError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class LoadSelectionHistory(
        cause: Throwable? = null
    ) : DiaryItemTitleSelectionHistoryError(
        "日記項目タイトル選択履歴の読込に失敗しました。",
        cause
    )

    class DeleteSelectionHistoryItem(
        cause: Throwable? = null
    ) : DiaryItemTitleSelectionHistoryError(
        "日記項目タイトル選択履歴アイテムの削除に失敗しました。",
        cause
    )
}
