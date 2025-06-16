package com.websarva.wings.android.zuboradiary.domain.usecase.settings.error

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseError

internal sealed class IsWeatherInfoAcquisitionEnabledError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class LoadSettings(
        cause: Throwable? = null
    ) : IsWeatherInfoAcquisitionEnabledError(
        "天気情報取得設定読込に失敗しました。",
        cause
    )
}
