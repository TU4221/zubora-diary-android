package com.websarva.wings.android.zuboradiary.domain.usecase.diary.list.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseUnknownException
import com.websarva.wings.android.zuboradiary.domain.usecase.diary.list.LoadExistingDiaryDateListUseCase

/**
 * [LoadExistingDiaryDateListUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class ExistingDiaryDateListLoadException(
    message: String,
    cause: Throwable
) : UseCaseException(message, cause) {

    /**
     * 日記が存在する日付のリストの読み込みが失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class LoadFailure(
        cause: Throwable
    ) : ExistingDiaryDateListLoadException(
        "日記が存在する日付のリストの読込に失敗しました。",
        cause
    )

    /**
     * 予期せぬエラーが発生した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class Unknown(
        cause: Throwable
    ) : ExistingDiaryDateListLoadException(
        "予期せぬエラーが発生しました。",
        cause
    ), UseCaseUnknownException
}
