package com.websarva.wings.android.zuboradiary.data.preferences.exception

/**
 * ストレージの空き容量が不足しているためにデータストアへのデータの書き込みに失敗したことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class DataStoreInsufficientStorageException(
    cause: Throwable? = null
) : DataStoreException("ストレージの空き容量が不足しています。", cause)
