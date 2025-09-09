package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadNewDiaryListUseCase

/**
 * [LoadNewDiaryListUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class DiaryListNewLoadException (
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 日記リストの新規読み込みに失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class LoadFailure(
        cause: Throwable
    ) : DiaryListNewLoadException("日記リストの新規読込に失敗しました。", cause)

    /**
     * 日記リストのフッターの更新に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class FooterUpdateFailure(
        cause: Throwable
    ) : DiaryListNewLoadException("日記リストのフッターの更新に失敗しました。", cause)
}
