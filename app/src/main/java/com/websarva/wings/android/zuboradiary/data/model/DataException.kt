package com.websarva.wings.android.zuboradiary.data.model

internal open class DataException (
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)
