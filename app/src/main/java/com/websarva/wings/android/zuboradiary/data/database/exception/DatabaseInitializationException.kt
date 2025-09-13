package com.websarva.wings.android.zuboradiary.data.database.exception

/**
 * データベースの初期化に失敗したことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class DatabaseInitializationException(
    cause: Throwable? = null
) : DatabaseException("データベースの初期化に失敗しました。", cause)
