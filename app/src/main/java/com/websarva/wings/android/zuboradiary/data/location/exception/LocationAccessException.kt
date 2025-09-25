package com.websarva.wings.android.zuboradiary.data.location.exception

/**
 * 位置情報へのアクセスに失敗したことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class LocationAccessException(
    cause: Throwable? = null
) : LocationProviderException("位置情報へのアクセスに失敗", cause)
