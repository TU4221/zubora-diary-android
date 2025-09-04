package com.websarva.wings.android.zuboradiary.domain.usecase.exception

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

/**
 * ユースケース層で発生する例外の基底となる抽象クラス。
 *
 * ユースケースの実行中に、ビジネスロジックに関連する何らかの問題が発生したことを示す。
 *
 * @param message 例外メッセージ。エラーの概要を説明する。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 *              ドメイン層で特定の状況を表現するために直接この例外のサブクラスがインスタンス化され、
 *              ラップする下位層の例外が存在しない場合は `null` となることがある。
 */
internal abstract class UseCaseException(
    message: String,
    cause: Throwable?
) : DomainException(message, cause)

