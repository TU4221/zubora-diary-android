package com.websarva.wings.android.zuboradiary.domain.exception.diary

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class LoadDiaryFailedException (
    message: String,
    cause: Throwable
) : DomainException(message, cause)
