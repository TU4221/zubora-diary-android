package com.websarva.wings.android.zuboradiary.domain.usecase.diary.list.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseUnknownException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.list.RefreshDiaryListUseCase

/**
 * [RefreshDiaryListUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class DiaryListRefreshException (
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 既存の日記リストの再読み込みに失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class RefreshFailure(
        cause: Throwable
    ) : DiaryListRefreshException("既存の日記リストの再読み込みに失敗しました。", cause)

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        cause: Throwable
    ) : DiaryListRefreshException(
        "予期せぬエラーが発生しました。",
        cause
    ), UseCaseUnknownException
}
