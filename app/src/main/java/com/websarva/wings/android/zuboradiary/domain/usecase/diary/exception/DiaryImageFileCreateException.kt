package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.CreateDiaryImageFileUseCase

/**
 * [CreateDiaryImageFileUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]、または `null`。
 */
internal sealed class DiaryImageFileCreateException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 画像ファイルの作成に失敗した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class CreateFailure(
        cause: Throwable
    ) : DiaryImageFileCreateException(
        "画像ファイルの作成に失敗しました。",
        cause
    )
}
