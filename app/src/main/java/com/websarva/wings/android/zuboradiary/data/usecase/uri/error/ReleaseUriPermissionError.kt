package com.websarva.wings.android.zuboradiary.data.usecase.uri.error

import com.websarva.wings.android.zuboradiary.data.usecase.UseCaseError

internal sealed class ReleaseUriPermissionError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class ReleaseUriPermission(
        cause: Throwable?
    ) : ReleaseUriPermissionError(
        "Uri権限解放に失敗しました。",
        cause
    )

    class CheckUriUsage(
        cause: Throwable?
    ) : ReleaseUriPermissionError(
        "Uri使用確認に失敗しました。",
        cause
    )
}
