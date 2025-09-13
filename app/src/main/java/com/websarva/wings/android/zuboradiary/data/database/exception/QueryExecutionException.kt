package com.websarva.wings.android.zuboradiary.data.database.exception

/**
 * データベースのクエリ実行に失敗したことを示す例外。
 *
 * @param queryDescription 失敗したクエリの説明。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class QueryExecutionException(
    queryDescription: String,
    cause: Throwable? = null
) : DatabaseException("クエリの実行に失敗しました。 ($queryDescription)", cause)
