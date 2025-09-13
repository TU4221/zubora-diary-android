package com.websarva.wings.android.zuboradiary.data.preferences.exception

/**
 * データストアからのデータの読み込みに失敗したことを示す例外。
 *
 * @param keyName 読み込みに失敗したキーの名前。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class ReadException(
    keyName: String? = null,
    cause: Throwable? = null
) : DataStoreException("DataStoreからの読み込みに失敗しました${keyName?.let { " (キー: $it)" } ?: ""}", cause)
