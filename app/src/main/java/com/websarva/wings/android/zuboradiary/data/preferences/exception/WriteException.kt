package com.websarva.wings.android.zuboradiary.data.preferences.exception

/**
 * データストアへのデータの書き込みに失敗したことを示す例外。
 *
 * @param keyName 書き込みに失敗したキーの名前。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class WriteException(
    keyName: String? = null,
    cause: Throwable? = null
) : DataStoreException("DataStoreへの書き込みに失敗しました${keyName?.let { " (キー: $it)" } ?: ""}", cause)
