package com.websarva.wings.android.zuboradiary.data.database.exception

/**
 * データベースが破損していることを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class DatabaseCorruptionException(
    cause: Throwable? = null
) : DatabaseException("データベースが破損しています。", cause)
