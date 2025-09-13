package com.websarva.wings.android.zuboradiary.data.exception

/**
 * 永続化ストレージ (データベース、ファイルシステム、DataStoreなど) への
 * アクセスや操作に関連する問題を示す抽象例外。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal abstract class StorageException(
    message: String,
    cause: Throwable? = null
) : DataLayerException(message, cause)
