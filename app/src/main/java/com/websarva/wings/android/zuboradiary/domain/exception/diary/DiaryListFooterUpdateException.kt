package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.UpdateDiaryListFooterUseCase

/**
 * [UpdateDiaryListFooterUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class DiaryListFooterUpdateException (
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 日記リストのフッターの更新が失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class UpdateFailure (
        cause: Throwable
    ) : DiaryListFooterUpdateException("日記リストのフッターを更新に失敗しました。", cause)
}
