package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class DiaryLoadFailureException (
    message: String,
    cause: Throwable
) : DomainException(message, cause)
