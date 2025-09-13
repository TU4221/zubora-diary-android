package com.websarva.wings.android.zuboradiary.data.network.exception

/**
 * レスポンスデータのパース (JSONなどからオブジェクトへの変換) に失敗したことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class ResponseParsingException(
    cause: Throwable? = null
) : NetworkOperationException("ネットワークレスポンスの解析に失敗しました", cause)
