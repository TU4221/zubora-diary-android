package com.websarva.wings.android.zuboradiary.data.model

internal sealed class UseCaseResult<out T> {
    data class Success<out T>(val value: T) : UseCaseResult<T>()
    data class Error(val exception: Exception) : UseCaseResult<Nothing>()
}
