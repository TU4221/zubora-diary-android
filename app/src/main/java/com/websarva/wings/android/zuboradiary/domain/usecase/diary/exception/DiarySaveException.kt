package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseUnknownException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.SaveDiaryUseCase
import java.time.LocalDate

/**
 * [SaveDiaryUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class DiarySaveException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 日記データの保存に失敗した場合にスローされる例外。
     *
     * @param date 保存しようとした日記の日付。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class SaveFailure(
        date: LocalDate,
        cause: Throwable
    ) : DiarySaveException("日付 '$date' の日記の保存に失敗しました。", cause)

    /**
     * ストレージ容量不足により、日記データの保存に失敗した場合にスローされる例外。
     *
     * @param date 保存しようとした日記の日付。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class InsufficientStorage(
        date: LocalDate,
        cause: Throwable
    ) : DiarySaveException("ストレージ容量不足により、日付 '$date' の日記の保存に失敗しました。", cause)

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        cause: Throwable
    ) : DiarySaveException(
        "予期せぬエラーが発生しました。",
        cause
    ), UseCaseUnknownException
}
