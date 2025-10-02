package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CacheDiaryImageUseCase

/**
 * [CacheDiaryImageUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]、または `null`。
 */
internal sealed class DiaryImageCacheException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 日記用の画像のキャッシュに失敗した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class CacheFailure(
        cause: Throwable
    ) : DiaryImageCacheException(
        "画像ファイルのキャッシュに失敗しました。",
        cause
    )

    /**
     * ストレージ容量不足により、日記用の画像のキャッシュに失敗した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class InsufficientStorage(
        cause: Throwable
    ) : DiaryImageCacheException(
        "ストレージ容量不足により、画像ファイルのキャッシュに失敗しました。",
        cause
    )

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        cause: Throwable
    ) : DiaryImageCacheException(
        "予期せぬエラーが発生しました。",
        cause
    )
}
