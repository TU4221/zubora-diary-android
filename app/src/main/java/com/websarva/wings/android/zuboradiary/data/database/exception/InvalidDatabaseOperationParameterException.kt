package com.websarva.wings.android.zuboradiary.data.database.exception

/**
 * データベース操作をする際に、無効なパラメータが指定されたことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class InvalidDatabaseOperationParameterException(
    cause: Throwable? = null
) : DatabaseException("データベース操作のパラメータが不正。", cause)
