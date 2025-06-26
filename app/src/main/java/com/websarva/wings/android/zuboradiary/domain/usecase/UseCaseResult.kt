package com.websarva.wings.android.zuboradiary.domain.usecase

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal sealed class UseCaseResult<out T, out E : DomainException> {
    data class Success<out T>(val value: T) : UseCaseResult<T, Nothing>()
    data class Error<out E : DomainException>(val error: E) : UseCaseResult<Nothing, E>()
}
