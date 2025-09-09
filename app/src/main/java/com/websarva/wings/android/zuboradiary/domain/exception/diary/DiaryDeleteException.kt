package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException
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
     * 特定の日付の日記の削除に失敗した場合にスローされる例外。
     *
     * @param date 削除しようとした日記の日付。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class DeleteFailure (
        date: LocalDate,
        cause: Throwable
    ) : DiaryDeleteException("日付 '$date' の日記の削除に失敗しました。", cause)

    // TODO:Uri権限の取り消しに失敗しても日記保存がメインの為、成功とみなす。その為、削除かViewmodelで無視するようにするか件検討
    /**
     * 指定された画像URI権限解放に失敗した場合にスローされる例外。
     *
     * @param uriString 権限解放対象の画像URIの文字列。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class PermissionReleaseFailure(
        uriString: String,
        cause: Throwable
    ) : DiaryDeleteException("画像URI '$uriString' の権限解放に失敗しました。", cause)
}
