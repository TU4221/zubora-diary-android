package com.websarva.wings.android.zuboradiary.domain.exception

/**
 * パラメータが無効だった場合の例外。
 *
 *  @param message この例外に関する詳細メッセージ。エラーの原因や状況を簡潔に説明する。
 *                デフォルトは「無効なパラメータ。」
 * @param cause この例外の根本原因となった[Throwable]。データ層で発生したパラメータ関連例外を含む。
 */
internal class InvalidParameterException(
    message: String = "無効なパラメータ。",
    cause: Throwable? = null
) : DomainException(message, cause)
