package com.websarva.wings.android.zuboradiary.data.network.exception

/**
 * ネットワークへリクエストをする際に、無効なパラメータが指定されたことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class InvalidNetworkRequestParameterException(
    cause: Throwable? = null
) : NetworkOperationException("ネットワークへリクエストのパラメータが不正。", cause)
