package com.websarva.wings.android.zuboradiary.data.location.exception

/**
 * 位置情報の取得にタイムアウトした場合、または利用可能な位置情報がないことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class LocationUnavailableException(
    cause: Throwable? = null
) : LocationProviderException("現在、位置情報を利用できないか、タイムアウトしました", cause)
