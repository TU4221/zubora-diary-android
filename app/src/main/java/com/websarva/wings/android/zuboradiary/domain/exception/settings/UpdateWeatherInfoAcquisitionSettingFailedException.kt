package com.websarva.wings.android.zuboradiary.domain.exception.settings

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class UpdateWeatherInfoAcquisitionSettingFailedException(
    isEnabled: Boolean,
    cause: Throwable
) : DomainException(
    "パスコード設定 '${
        if (isEnabled) {
            "有効"
        } else {
            "無効"
        }
    }' の更新に失敗しました。"
    , cause
)
