package com.websarva.wings.android.zuboradiary.data.database.exception

/**
 * データベースのマイグレーションに失敗したことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class DatabaseMigrationException(
    cause: Throwable? = null
) : DatabaseException("データベースのマイグレーションに失敗しました。", cause)
