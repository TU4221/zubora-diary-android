package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.ClearDiaryImageCacheFileUseCase

/**
 * [ClearDiaryImageCacheFileUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]、または `null`。
 */
internal sealed class DiaryImageCacheFileClearException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * キャッシュファイルのクリアに失敗した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class ClearFailure(
        cause: Throwable
    ) : DiaryImageCacheFileClearException(
        "日記画像キャッシュファイルのクリアに失敗しました。",
        cause
    )
}
