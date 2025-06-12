package com.websarva.wings.android.zuboradiary.data.usecase.settings.error

import com.websarva.wings.android.zuboradiary.data.usecase.UseCaseError

internal sealed class IsWeatherInfoAcquisitionEnabledError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class LoadSettings(
        cause: Throwable? = null
    ) : IsWeatherInfoAcquisitionEnabledError(
        "天気情報取得設定確認に失敗しました。",
        cause
    )
}
