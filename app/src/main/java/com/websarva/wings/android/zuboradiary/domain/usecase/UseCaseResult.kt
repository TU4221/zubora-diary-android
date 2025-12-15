package com.websarva.wings.android.zuboradiary.domain.usecase

/**
 * ユースケースの実行結果。
 *
 * 成功時には [Success] を、失敗時には [Failure] を返し、
 * それぞれ結果の値またはユースケース例外を保持する。
 * これにより、ユースケースの呼び出し元は結果を型安全に処理できる。
 *
 * @param T 成功時の結果の型。
 * @param E 失敗時のドメイン例外の型。 [UseCaseException] のサブクラスである必要がある。
 */
internal sealed interface UseCaseResult<out T, out E : UseCaseException> {
    /**
     * ユースケースの実行が成功したことを表す。
     *
     * @param value 成功した結果の値。
     * @param T 成功時の結果の型。
     */
    data class Success<out T>(val value: T) : UseCaseResult<T, Nothing>

    /**
     * ユースケースの実行が失敗したことを表す。
     *
     * @param exception 発生したドメイン例外。
     * @param E 失敗時のドメイン例外の型。
     */
    data class Failure<out E : UseCaseException>(val exception: E) : UseCaseResult<Nothing, E>
}
