package com.websarva.wings.android.zuboradiary.data.location.exception

import com.websarva.wings.android.zuboradiary.data.exception.DeviceFeatureAccessException

/**
 * 位置情報サービス (FusedLocationProviderClient) に関連する問題を示す例外。
 * [DeviceFeatureAccessException] を継承。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal open class LocationProviderException(
    message: String,
    cause: Throwable? = null
) : DeviceFeatureAccessException(message, cause)
