package com.websarva.wings.android.zuboradiary.domain.usecase

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

/**
 * ユースケースの実行結果を表す基底クラス。
 *
 * 成功時には [Success] を、失敗時には [Failure] を返し、
 * それぞれ結果の値またはドメイン固有の例外を保持する。
 * これにより、ユースケースの呼び出し元は結果を型安全に処理できる。
 *
 * @param T 成功時の結果の型。
 * @param E 失敗時のドメイン例外の型。 [DomainException] またはそのサブクラスである必要がある。
 */
internal sealed class UseCaseResult<out T, out E : DomainException> {
    /**
     * ユースケースの実行が成功したことを表す。
     *
     * @param value 成功した結果の値。
     * @param T 成功時の結果の型。
     */
    data class Success<out T>(val value: T) : UseCaseResult<T, Nothing>()

    /**
     * ユースケースの実行が失敗したことを表す。
     *
     * @param exception 発生したドメイン例外。
     * @param E 失敗時のドメイン例外の型。
     */
    data class Failure<out E : DomainException>(val exception: E) : UseCaseResult<Nothing, E>()
}

/**
 * [DomainException] をエラー型とする [UseCaseResult] の型エイリアス。
 * ユースケースが汎用的なドメインエラーを返す場合に使用する。
 *
 * @param T 成功時の結果の型。
 */
internal typealias DefaultUseCaseResult<T> = UseCaseResult<T, DomainException>
