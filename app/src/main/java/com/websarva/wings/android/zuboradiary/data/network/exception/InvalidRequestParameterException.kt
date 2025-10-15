package com.websarva.wings.android.zuboradiary.data.network.exception

/**
 * ネットワークリクエストを生成する際に、無効なパラメータが指定されたことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class InvalidRequestParameterException(
    cause: Throwable? = null
) : NetworkOperationException("ネットワークリクエストのパラメータが不正。", cause)
