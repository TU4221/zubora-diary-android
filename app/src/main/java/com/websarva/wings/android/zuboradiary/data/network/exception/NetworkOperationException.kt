package com.websarva.wings.android.zuboradiary.data.network.exception

import com.websarva.wings.android.zuboradiary.data.exception.NetworkException

/**
 * ネットワーク操作 (Retrofit) に関連する問題を示す例外。
 * [NetworkException] を継承。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal open class NetworkOperationException(
    message: String,
    cause: Throwable? = null
) : NetworkException(message, cause)
