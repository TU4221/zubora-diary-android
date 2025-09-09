package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadAdditionDiaryListUseCase

/**
 * [LoadAdditionDiaryListUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class DiaryListAdditionLoadException (
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 日記リストの追加読み込みに失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class LoadFailure(
        cause: Throwable
    ) : DiaryListAdditionLoadException("日記リストの追加読込に失敗しました。", cause)

    /**
     * 日記リストのフッターの更新に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class FooterUpdateFailure(
        cause: Throwable
    ) : DiaryListAdditionLoadException("日記リストのフッターの更新に失敗しました。", cause)
}
