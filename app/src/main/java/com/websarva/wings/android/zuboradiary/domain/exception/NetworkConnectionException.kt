package com.websarva.wings.android.zuboradiary.domain.exception

/**
 * ネットワーク(WebApiなど)への接続性に関連する問題が発生した場合の例外。
 *
 *  @param message この例外に関する詳細メッセージ。エラーの原因や状況を簡潔に説明する。
 *                デフォルトは「ネットワーク接続の問題が発生。」
 * @param cause この例外の根本原因となった[Throwable]。データ層で発生したネットワーク接続関連例外を含む。
 */
internal class NetworkConnectionException(
    message: String = "ネットワーク接続の問題が発生。",
    cause: Throwable?
) : DomainException(message, cause)
