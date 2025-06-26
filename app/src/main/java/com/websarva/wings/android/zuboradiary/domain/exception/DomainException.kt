package com.websarva.wings.android.zuboradiary.domain.exception


internal abstract class DomainException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
