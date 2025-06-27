package com.websarva.wings.android.zuboradiary.domain.usecase.exception

import java.time.LocalDate

internal sealed class FetchWeatherInfoUseCaseException(
    message: String,
    cause: Throwable? = null
) : UseCaseException(message, cause) {

    class LocationPermissionNotGranted : FetchWeatherInfoUseCaseException("位置情報取得権限が未取得です。")

    class AccessLocationFailed(
        cause: Throwable
    ) : FetchWeatherInfoUseCaseException(
        "位置情報取得に失敗しました。",
        cause
    )

    class WeatherInfoDateOutOfRange(
        date: LocalDate,
        cause: Throwable? = null
    ) : FetchWeatherInfoUseCaseException("指定した日付 '$date' は、天気情報を取得できる範囲を超えています。", cause)

    class FetchWeatherInfoFailed(
        cause: Throwable
    ) : FetchWeatherInfoUseCaseException(
        "天気情報取得に失敗しました。",
        cause
    )
}
