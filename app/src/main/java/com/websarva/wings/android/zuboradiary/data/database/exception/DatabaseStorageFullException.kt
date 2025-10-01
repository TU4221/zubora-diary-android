package com.websarva.wings.android.zuboradiary.data.database.exception

/**
 * データベース操作中にストレージ容量が不足したことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class DatabaseStorageFullException(
    cause: Throwable? = null
) : DatabaseException("データベースのストレージ容量が不足しています。", cause)
