package com.websarva.wings.android.zuboradiary.data.usecase.diary.error

import com.websarva.wings.android.zuboradiary.data.usecase.UseCaseError

internal sealed class DoesDiaryExistError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class CheckDiaryExistence(
        cause: Throwable? = null
    ) : DoesDiaryExistError(
        "日記既存確認に失敗しました。",
        cause
    )
}
