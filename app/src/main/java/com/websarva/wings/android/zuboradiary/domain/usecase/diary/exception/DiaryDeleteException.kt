package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.model.ImageFileName
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.DeleteDiaryUseCase
import java.time.LocalDate

/**
 * [DeleteDiaryUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class DiaryDeleteException (
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 特定の日付の日記データの削除に失敗した場合にスローされる例外。
     *
     * @param date 削除しようとした日記の日付。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class DiaryDataDeleteFailure (
        date: LocalDate,
        cause: Throwable
    ) : DiaryDeleteException("日付 '$date' の日記データの削除に失敗しました。", cause)

    /**
     * 削除対象の日記データみつからなかった場合にスローされる例外。
     *
     * @param date 削除しようとした日記の日付。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class DiaryDataNotFound (
        date: LocalDate,
        cause: Throwable
    ) : DiaryDeleteException("削除対象である日付 '$date' の日記データがみつかりませんでした。", cause)

    /**
     * 日記に添付された画像ファイルの削除に失敗した場合にスローされる例外。
     *
     * @param date 削除しようとした日記の日付。
     * @param fileName 削除しようとした画像ファイル名。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class DiaryImageFileDeleteFailure(
        date: LocalDate,
        fileName: ImageFileName,
        cause: Throwable
    ) : DiaryDeleteException("削除対象である日付 '$date' の日記の画像ファイル '$fileName' の削除に失敗しました。", cause)
}
