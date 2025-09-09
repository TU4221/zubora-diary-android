package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DoesDiaryExistUseCase
import java.time.LocalDate

/**
 * [DoesDiaryExistUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class DiaryExistenceCheckFailureException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 特定の日付の日記の存在確認に失敗した場合にスローされる例外。
     *
     * @param date 存在確認をしようとした日記の日付。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class CheckFailure(
        date: LocalDate,
        cause: Throwable
    ) : DiaryExistenceCheckFailureException("日付 '$date' の日記の存在確認に失敗しました。", cause)
}
