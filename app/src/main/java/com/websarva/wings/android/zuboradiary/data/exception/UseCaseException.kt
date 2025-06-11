package com.websarva.wings.android.zuboradiary.data.exception

internal open class UseCaseException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
