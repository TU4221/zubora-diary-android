package com.websarva.wings.android.zuboradiary.domain.usecase.uri.error

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseError

internal sealed class TakeUriPermissionError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class TakeUriPermission(
        cause: Throwable?
    ) : TakeUriPermissionError(
        "Uri権限取得に失敗しました。",
        cause
    )
}
