package com.websarva.wings.android.zuboradiary.data.database.exception

import com.websarva.wings.android.zuboradiary.data.exception.StorageException

/**
 * データベース操作 (Room) に関連する問題を示す例外。
 * [StorageException] を継承。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal open class DatabaseException(
    message: String,
    cause: Throwable? = null
) : StorageException(message, cause)
