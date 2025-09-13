package com.websarva.wings.android.zuboradiary.data.location.exception

/**
 * 位置情報へのアクセス権限がないことを示す例外。
 *
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]。
 */
internal class PermissionDeniedException(
    cause: Throwable? = null
) : LocationProviderException("位置情報へのアクセス権限がありません", cause)
