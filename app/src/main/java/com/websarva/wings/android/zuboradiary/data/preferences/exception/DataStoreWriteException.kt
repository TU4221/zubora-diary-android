package com.websarva.wings.android.zuboradiary.data.preferences.exception

/**
 * データストアへのデータの書き込みに失敗したことを示す例外。
 *
 * @param keyName 書き込みに失敗したキーの名前。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class DataStoreWriteException(
    keyName: String? = null,
    cause: Throwable? = null
) : DataStoreException("データストアへの書き込みに失敗しました${keyName?.let { " (キー: $it)" } ?: ""}", cause)
