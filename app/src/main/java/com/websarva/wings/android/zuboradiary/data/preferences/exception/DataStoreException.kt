package com.websarva.wings.android.zuboradiary.data.preferences.exception

import com.websarva.wings.android.zuboradiary.data.exception.StorageException

// TODO:サブクラス未使用(既存例外と置き換え必須。サブクラスは仮のため置き換え時に内容を確認すること)
/**
 * データストア操作 (DataStore Preferences) 操作に関連する問題を示す例外。
 * [StorageException] を継承。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal open class DataStoreException(
    message: String,
    cause: Throwable? = null
) : StorageException(message, cause)
