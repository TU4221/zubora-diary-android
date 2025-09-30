package com.websarva.wings.android.zuboradiary.data.database.exception

/**
 * 読み込み対象のレコードの更新に失敗したしたことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class RecordNotFoundException(
    cause: Throwable? = null
) : DatabaseException("読み込み対象のレコードが見つかりませんでした。", cause)
