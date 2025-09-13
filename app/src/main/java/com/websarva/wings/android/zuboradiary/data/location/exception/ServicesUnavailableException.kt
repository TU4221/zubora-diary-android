package com.websarva.wings.android.zuboradiary.data.location.exception

/**
 * Google Play Services が利用できない、またはバージョンが古いことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class ServicesUnavailableException(
    cause: Throwable? = null
) : LocationProviderException("位置情報のためのServicesが利用できないか、バージョンが古いです", cause)
