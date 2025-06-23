package com.websarva.wings.android.zuboradiary.domain.usecase.uri.error

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseError


internal sealed class UriError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class TakePermission(
        cause: Throwable?
    ) : UriError(
        "Uri権限取得に失敗しました。",
        cause
    )

    class ReleasePermission(
        cause: Throwable?
    ) : UriError(
        "Uri権限解放に失敗しました。",
        cause
    )
}
