package com.websarva.wings.android.zuboradiary.data.network.exception

/**
 * HTTPエラーレスポンス (4xx, 5xx) を受信したことを示す例外。
 *
 * @param statusCode HTTPステータスコード
 * @param message エラーメッセージ
 * @param errorBody エラーレスポンスのボディ (オプション)
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class HttpException(
    statusCode: Int,
    message: String,
    errorBody: String? = null,
    cause: Throwable? = null
) : NetworkOperationException(
    "HTTP $statusCode: $message" +
            if (!errorBody.isNullOrBlank()) {
                ". Error Body: $errorBody"
            } else {
                ""
            },
    cause
)
