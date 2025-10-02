package com.websarva.wings.android.zuboradiary.domain.exception

/**
 * データベース、ファイルシステムなどで使用するデータストレージの空き容量が不足しているために問題が発生した場合の例外。
 *
 *  @param message この例外に関する詳細メッセージ。エラーの原因や状況を簡潔に説明する。
 *                デフォルトは「データストレージの空き容量が不足」
 * @param cause この例外の根本原因となった[Throwable]。データ層で発生したデータストレージ関連例外を含む。
 */
internal class InsufficientStorageException(
    message: String = "データストレージの空き容量が不足。",
    cause: Throwable? = null
) : DataStorageException(message, cause)
