package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadNewestDiaryUseCase

/**
 * [LoadNewestDiaryUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。nullの場合もある。
 */
internal sealed class NewestDiaryLoadException (
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 最新の日記の読み込みに失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class LoadFailure(
        cause: Throwable
    ) : NewestDiaryLoadException("最新の日記の読込に失敗しました。", cause)

    /**
     * 最新の日記が見つからなかった場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class DataNotFound(
        cause: Throwable
    ) : NewestDiaryLoadException("最新の日記が見つかりませんでした。", cause)
}
