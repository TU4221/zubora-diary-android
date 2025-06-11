package com.websarva.wings.android.zuboradiary.data.model

// TODO:仮クラス名
internal sealed class UseCaseResult2<out T, out E> {
    data class Success<out T>(val value: T) : UseCaseResult2<T, Nothing>()
    data class Error<out E>(val exception: E) : UseCaseResult2<Nothing, E>()
}
