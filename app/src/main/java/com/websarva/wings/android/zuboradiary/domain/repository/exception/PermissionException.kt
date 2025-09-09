package com.websarva.wings.android.zuboradiary.domain.repository.exception

/**
 * 権限 (リソースなど) の操作に問題が発生した場合の例外。
 *
 *  @param message この例外に関する詳細メッセージ。エラーの原因や状況を簡潔に説明する。
 *                デフォルトは「権限の操作に問題が発生。」
 * @param cause この例外の根本原因となった[Throwable]。データ層で発生した権限関連例外を含む。
 */
internal class PermissionException(
    message: String = "権限の操作に問題が発生。",
    cause: Throwable?
) : RepositoryException(message, cause)
