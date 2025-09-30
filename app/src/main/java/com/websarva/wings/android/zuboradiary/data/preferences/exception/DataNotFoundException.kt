package com.websarva.wings.android.zuboradiary.data.preferences.exception

/**
 * データストアから対象のキーのデータが見つからなかったことを示す例外。
 *
 * @param keyName 見つからなかったデータのキーの名前。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class DataNotFoundException(
    keyName: String? = null,
    cause: Throwable? = null
) : DataStoreException("データストアから対象のキーのデータが見つかりませんでした。${keyName?.let { " (キー: $it)" } ?: ""}", cause)
