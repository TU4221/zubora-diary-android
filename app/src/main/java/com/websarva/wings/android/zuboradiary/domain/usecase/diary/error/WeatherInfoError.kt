package com.websarva.wings.android.zuboradiary.domain.usecase.diary.error

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseError

internal sealed class WeatherInfoError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class LoadWeatherInfo(
        cause: Throwable? = null
    ) : WeatherInfoError(
        "天気情報取得に失敗しました。",
        cause
    )
}
