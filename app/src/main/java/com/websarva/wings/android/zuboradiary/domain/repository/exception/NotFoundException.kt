package com.websarva.wings.android.zuboradiary.domain.repository.exception

/**
 * 要求されたデータが見つからなかった場合の例外。
 *
 *  @param message この例外に関する詳細メッセージ。エラーの原因や状況を簡潔に説明する。
 *                デフォルトは「要求されたデータが見つかりません。」
 * @param cause この例外の根本原因となった[Throwable]。データ層で発生したデータ関連例外を含む。
 */
internal class NotFoundException(
    message: String = "要求されたデータが見つかりません。",
    cause: Throwable? = null
) : RepositoryException(message, cause)
