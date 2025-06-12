package com.websarva.wings.android.zuboradiary.data.usecase.diary.error

import com.websarva.wings.android.zuboradiary.data.usecase.UseCaseError

internal sealed class ShouldRequestDiaryUpdateConfirmationError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class CheckDiaryExistence(
        cause: Throwable? = null
    ) : ShouldRequestDiaryUpdateConfirmationError(
        "日記既存確認に失敗しました。",
        cause
    )
}
