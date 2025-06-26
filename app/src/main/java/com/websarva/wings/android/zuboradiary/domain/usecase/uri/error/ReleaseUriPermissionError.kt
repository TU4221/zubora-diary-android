package com.websarva.wings.android.zuboradiary.domain.usecase.uri.error

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseError

internal sealed class ReleaseUriPermissionError(
    message: String,
    cause: Throwable
) : UseCaseError(message, cause) {

    class ReleaseUriPermission(
        cause: Throwable
    ) : ReleaseUriPermissionError(
        "Uri権限解放に失敗しました。",
        cause
    )

    class CheckUriUsage(
        cause: Throwable
    ) : ReleaseUriPermissionError(
        "Uri使用確認に失敗しました。",
        cause
    )
}
