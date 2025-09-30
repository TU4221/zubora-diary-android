package com.websarva.wings.android.zuboradiary.domain.exception

/**
 * ロールバック処理に問題が発生した場合の例外。
 *
 *  @param message この例外に関する詳細メッセージ。エラーの原因や状況を簡潔に説明する。
 *                デフォルトは「ロールバック処理に問題が発生。」
 * @param cause この例外の根本原因となった[Throwable]。
 */
internal class RollbackException(
    message: String = "ロールバック処理に問題が発生。",
    cause: Throwable? = null
) : DomainException(message, cause)
