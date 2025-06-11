package com.websarva.wings.android.zuboradiary.data.exception

internal open class UriException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

internal class TakeUriPermissionFailedException(cause: Throwable? = null) :
    UriException(
        "Uriの権限取得に失敗しました。",
        cause
    )

internal class ReleaseUriPermissionFailedException(cause: Throwable? = null) :
    UriException(
        "Uriの権限解放に失敗しました。",
        cause
    )
