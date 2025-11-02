package com.websarva.wings.android.zuboradiary.domain.usecase.diary.exception

import com.websarva.wings.android.zuboradiary.domain.model.diary.DiaryId
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseUnknownException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.LoadDiaryByIdUseCase

/**
 * [LoadDiaryByIdUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。nullの場合もある。
 */
internal sealed class DiaryLoadByIdException (
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 日記の読み込みに失敗した場合にスローされる例外。
     *
     * @param id 読み込みに失敗した日記のID。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class LoadFailure(
        id: DiaryId,
        cause: Throwable
    ) : DiaryLoadByIdException("指定されたID '${id.value}' の日記の読込に失敗しました。", cause)

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        cause: Throwable
    ) : DiaryLoadByIdException(
        "予期せぬエラーが発生しました。",
        cause
    ), UseCaseUnknownException
}
