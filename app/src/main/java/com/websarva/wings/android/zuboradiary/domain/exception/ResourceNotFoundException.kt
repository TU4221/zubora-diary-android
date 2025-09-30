package com.websarva.wings.android.zuboradiary.domain.exception

/**
 * 要求されたリソースが見つからなかった場合の例外。
 *
 *  @param message この例外に関する詳細メッセージ。エラーの原因や状況を簡潔に説明する。
 *                デフォルトは「要求されたリソースが見つかりません。」
 * @param cause この例外の根本原因となった[Throwable]。データ層で発生したリソース関連例外を含む。
 */
internal class ResourceNotFoundException(
    message: String = "要求されたリソースが見つかりません。",
    cause: Throwable? = null
) : DomainException(message, cause)
