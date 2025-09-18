package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

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
    class SaveDiaryDataFailure(
        date: LocalDate,
        cause: Throwable
    ) : DiarySaveException("日付 '$date' の日記の保存に失敗しました。", cause)

    /**
     * ストレージに保存された画像ファイルの更新に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class StorageImageFileUpdateFailure(
        cause: Throwable
    ) : DiarySaveException("ストレージに保存された画像ファイルの更新に失敗しました。", cause)

    /**
     * 日記項目タイトル選択履歴の更新に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class SaveDiaryItemTileSelectionHistoryFailure(
        cause: Throwable
    ) : DiarySaveException("日記項目タイトル選択履歴の更新に失敗しました。", cause)
}
