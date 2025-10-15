package com.websarva.wings.android.zuboradiary.data.network.exception

/**
 * ネットワーク操作をする際に、無効なパラメータが指定されたことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class InvalidNetworkOperationParameterException(
    cause: Throwable? = null
) : NetworkOperationException("ネットワーク操作のパラメータが不正。", cause)
