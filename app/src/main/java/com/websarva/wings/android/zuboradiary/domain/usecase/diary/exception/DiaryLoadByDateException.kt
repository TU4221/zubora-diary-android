package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByDateUseCase
import java.time.LocalDate

/**
 * [LoadDiaryByDateUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。nullの場合もある。
 */
internal sealed class DiaryLoadByDateException (
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 日記の読み込みに失敗した場合にスローされる例外。
     *
     * @param date 読み込みに失敗した日記の日付。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class LoadFailure(
        date: LocalDate,
        cause: Throwable
    ) : DiaryLoadByDateException("指定された日付 '$date' の日記の読込に失敗しました。", cause)

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        cause: Throwable
    ) : DiaryLoadByDateException(
        "予期せぬエラーが発生しました。",
        cause
    )
}
