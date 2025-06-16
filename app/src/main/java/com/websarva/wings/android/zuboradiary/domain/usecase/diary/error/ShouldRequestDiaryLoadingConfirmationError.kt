package com.websarva.wings.android.zuboradiary.domain.usecase.diary.error

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseError

internal sealed class ShouldRequestDiaryLoadingConfirmationError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class CheckDiaryExistence(
        cause: Throwable? = null
    ) : ShouldRequestDiaryLoadingConfirmationError(
        "日記既存確認に失敗しました。",
        cause
    )
}
