package com.websarva.wings.android.zuboradiary.data.usecase

internal sealed class UseCaseResult<out T, out E : UseCaseError> {
    data class Success<out T>(val value: T) : UseCaseResult<T, Nothing>()
    data class Error<out E : UseCaseError>(val error: E) : UseCaseResult<Nothing, E>()
}
