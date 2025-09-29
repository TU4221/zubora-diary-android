package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ShouldRequestDiaryLoadConfirmationUseCase

/**
 * [ShouldRequestDiaryLoadConfirmationUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class DiaryLoadConfirmationCheckException (
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 日記の読み込み確認ダイアログを表示する必要があるかどうかを判断するのに失敗した場合にスローされる例外クラス。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class CheckFailure (
        cause: Throwable
    ) : DiaryLoadConfirmationCheckException(
        "日記の読み込み確認ダイアログを表示する必要があるかどうかを判断するのに失敗しました。",
        cause
    )

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        cause: Throwable
    ) : DiaryLoadConfirmationCheckException(
        "予期せぬエラーが発生しました。",
        cause
    )
}
