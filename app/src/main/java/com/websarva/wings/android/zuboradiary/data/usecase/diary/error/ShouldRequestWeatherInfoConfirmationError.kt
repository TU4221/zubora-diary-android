package com.websarva.wings.android.zuboradiary.data.usecase.diary.error

import com.websarva.wings.android.zuboradiary.data.usecase.UseCaseError

internal sealed class ShouldRequestWeatherInfoConfirmationError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class LoadSettings(
        cause: Throwable? = null
    ) : ShouldRequestWeatherInfoConfirmationError(
        "天気情報取得設定読込に失敗しました。",
        cause
    )
}
