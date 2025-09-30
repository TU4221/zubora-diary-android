package com.websarva.wings.android.zuboradiary.data.database.exception

/**
 * データベースの状態が不正であったことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class DatabaseStateException(
    cause: Throwable? = null
) : DatabaseException("データベースの状態が不正です。", cause)
