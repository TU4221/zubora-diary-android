package com.websarva.wings.android.zuboradiary.domain.usecase.diary.list.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseUnknownException

/**
 * [com.websarva.wings.android.zuboradiary.domain.usecase.diary.list.LoadDiaryItemTitleSelectionHistoryListUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class DiaryItemTitleSelectionHistoryListLoadException(
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
    ) : DiaryItemTitleSelectionHistoryListLoadException(
        "日記項目タイトル選択履歴の読込に失敗しました。",
        cause
    )

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        cause: Throwable
    ) : DiaryItemTitleSelectionHistoryListLoadException(
        "予期せぬエラーが発生しました。",
        cause
    ), UseCaseUnknownException
}
