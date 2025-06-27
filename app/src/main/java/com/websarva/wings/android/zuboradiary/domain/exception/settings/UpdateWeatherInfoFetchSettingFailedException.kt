package com.websarva.wings.android.zuboradiary.domain.exception.settings

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class UpdateWeatherInfoFetchSettingFailedException(
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
