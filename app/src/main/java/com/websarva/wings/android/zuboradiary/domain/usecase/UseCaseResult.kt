package com.websarva.wings.android.zuboradiary.domain.usecase

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal sealed class UseCaseResult<out T, out E : DomainException> {
    data class Success<out T>(val value: T) : UseCaseResult<T, Nothing>()
    data class Failure<out E : DomainException>(val exception: E) : UseCaseResult<Nothing, E>()
}
// DomainException をエラー型とする場合の型エイリアス
internal typealias DefaultUseCaseResult<T> = UseCaseResult<T, DomainException>
