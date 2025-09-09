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
     * 日記の保存に失敗した場合にスローされる例外。
     *
     * @param date 保存しようとした日記の日付。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class SaveFailure(
        date: LocalDate,
        cause: Throwable
    ) : DiarySaveException("日付 '$date' の日記の保存に失敗しました。", cause)

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
    ) : DiarySaveException("画像URI '$uriString' の権限解放に失敗しました。", cause)

    // TODO:Uri権限の取り消しに失敗しても日記保存がメインの為、成功とみなす。その為、削除かViewmodelで無視するようにするか件検討
    /**
     * 指定された画像URI権限取得に失敗した場合にスローされる例外。
     *
     * @param uriString 権限取得対象の画像URIの文字列。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class PermissionTakeFailure(
        uriString: String,
        cause: Throwable
    ) : DiarySaveException("画像URI '$uriString' の権限取得に失敗しました。", cause)
}
