package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryUseCase
import java.time.LocalDate

/**
 * [LoadDiaryUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。nullの場合もある。
 */
internal sealed class DiaryLoadException (
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
    ) :DiaryLoadException("指定された日付 '$date' の日記の読込に失敗しました。", cause)

    /**
     * 日記が見つからなかった場合にスローされる例外。
     *
     * @param date 日記読込対象の日付。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class DataNotFound(
        date: LocalDate,
        cause: Throwable
    ) :DiaryLoadException("指定された日付 '$date' の日記が見つかりませんでした。", cause)
}
