package com.websarva.wings.android.zuboradiary.domain.exception.settings

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

/**
 * 天気情報取得設定の更新処理中に予期せぬエラーが発生した場合にスローされる例外。
 *
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal class WeatherInfoFetchSettingUpdateFailureException(
    isEnabled: Boolean,
    cause: Throwable
) : DomainException(
    "天気情報取得設定 '${
        if (isEnabled) {
            "有効"
        } else {
            "無効"
        }
    }' の更新に失敗しました。"
    , cause
)
