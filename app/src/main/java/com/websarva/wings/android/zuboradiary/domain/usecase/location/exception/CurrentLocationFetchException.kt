package com.websarva.wings.android.zuboradiary.domain.usecase.location.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.location.FetchCurrentLocationUseCase

/**
 * [FetchCurrentLocationUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]、または `null`。
 */
internal sealed class CurrentLocationFetchException(
    message: String,
    cause: Throwable? = null
) : UseCaseException(message, cause) {

    /**
     * 位置情報の取得権限が付与されていない場合の例外。
     */
    class LocationPermissionNotGranted : CurrentLocationFetchException("位置情報取得権限が未取得です。")

    /**
     * 位置情報の取得に失敗した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class LocationAccessFailure(
        cause: Throwable
    ) : CurrentLocationFetchException(
        "位置情報取得に失敗しました。",
        cause
    )
}
