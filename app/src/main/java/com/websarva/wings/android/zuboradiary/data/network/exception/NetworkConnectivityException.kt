package com.websarva.wings.android.zuboradiary.data.network.exception

/**
 * ネットワーク接続が利用できない、またはタイムアウトしたことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class NetworkConnectivityException(
    cause: Throwable? = null
) : NetworkOperationException("ネットワーク接続の問題またはタイムアウトが発生しました", cause)
