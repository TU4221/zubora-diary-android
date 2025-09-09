package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryItemTitleSelectionHistoryListUseCase

/**
 * [LoadDiaryItemTitleSelectionHistoryListUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class DiaryItemTitleSelectionHistoryLoadException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 日記項目のタイトル選択履歴の読み込みが失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class LoadFailure(
        cause: Throwable
    ) : DiaryItemTitleSelectionHistoryLoadException(
        "日記項目タイトル選択履歴の読込に失敗しました。",
        cause
    )
}
