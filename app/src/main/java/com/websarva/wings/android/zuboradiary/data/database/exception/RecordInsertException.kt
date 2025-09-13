package com.websarva.wings.android.zuboradiary.data.database.exception

/**
 * レコードの挿入に失敗したことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class RecordInsertException(
    cause: Throwable? = null
) : DatabaseException("レコードの挿入に失敗しました。", cause)
