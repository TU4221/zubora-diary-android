package com.websarva.wings.android.zuboradiary.domain.usecase.exception

internal sealed class AcquireWeatherInfoUseCaseException(
    message: String,
    cause: Throwable? = null
) : UseCaseException(message, cause) {

    class LocationPermissionNotGranted : AcquireWeatherInfoUseCaseException("位置情報取得権限が未取得です。")

    class AccessLocationFailed(
        cause: Throwable
    ) : AcquireWeatherInfoUseCaseException(
        "位置情報取得に失敗しました。",
        cause
    )

    class WeatherInfoDateOutOfRange :
        AcquireWeatherInfoUseCaseException("選択した日付は、天気情報を取得できる範囲を超えています。 ")

    class AcquireWeatherInfoFailed(
        cause: Throwable
    ) : AcquireWeatherInfoUseCaseException(
        "天気情報取得に失敗しました。",
        cause
    )
}
