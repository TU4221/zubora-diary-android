package com.websarva.wings.android.zuboradiary.data.database.exception

/**
 * レコードの更新に失敗したしたことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class RecordUpdateException(
    cause: Throwable? = null
) : DatabaseException("レコードの更新に失敗しました。", cause)
