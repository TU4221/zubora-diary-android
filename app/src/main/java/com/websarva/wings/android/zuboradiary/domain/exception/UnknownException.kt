package com.websarva.wings.android.zuboradiary.domain.exception

/**
 * 予期せぬエラーが発生した場合の例外。
 *
 *  @param message この例外に関する詳細メッセージ。エラーの原因や状況を簡潔に説明する。
 *                デフォルトは「予期せぬエラーが発生。」
 * @param cause この例外の根本原因となった[Throwable]。データ層で発生したデータストレージ関連例外を含む。
 */
internal open class UnknownException(
    message: String = "予期せぬエラーが発生。",
    cause: Throwable? = null
) : DomainException(message, cause)
