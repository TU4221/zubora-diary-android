package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ReleaseDiaryImageUriPermissionUseCase

/**
 * [ReleaseDiaryImageUriPermissionUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class DiaryImageUriPermissionReleaseException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 指定された画像URIが他の日記で使用されているかどうかの確認が失敗した場合にスローされる例外。
     *
     * @param uriString 確認対象の画像URIの文字列。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class ImageUriUsageCheckFailure(
        uriString: String,
        cause: Throwable
    ) : DiaryImageUriPermissionReleaseException("画像URI '$uriString' の使用確認に失敗しました。", cause)

    /**
     * 指定された画像URI権限解放に失敗した場合にスローされる例外。
     *
     * @param uriString 権限解放対象の画像URIの文字列。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class PermissionReleaseFailure(
        uriString: String,
        cause: Throwable
    ) : DiaryImageUriPermissionReleaseException("画像URI '$uriString' の権限解放に失敗しました。", cause)
}
