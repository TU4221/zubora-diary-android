package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CheckUnloadedDiariesExistUseCase

/**
 * [CheckUnloadedDiariesExistUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class UnloadedDiariesExistCheckException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 読み込まれていない日記が存在するかどうかを確認するのに失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class CheckFailure(
        cause: Throwable
    ) : UnloadedDiariesExistCheckException(
        "読み込まれていない日記が存在するかどうかを確認するのに失敗しました。",
        cause
    )
}
