package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CountDiariesUseCase

/**
 * [CountDiariesUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class DiaryCountException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 日記の総数を取得するのに失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class CountFailure(
        cause: Throwable
    ) : DiaryCountException("日記の総数の取得に失敗しました。", cause)
}
