package com.websarva.wings.android.zuboradiary.domain.usecase.exception

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal abstract class UseCaseException(
    message: String,
    cause: Throwable?
) : DomainException(message, cause)
