package com.websarva.wings.android.zuboradiary.data.usecase

internal open class UseCaseError(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
