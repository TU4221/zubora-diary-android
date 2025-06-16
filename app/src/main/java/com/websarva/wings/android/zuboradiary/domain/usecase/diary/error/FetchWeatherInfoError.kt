package com.websarva.wings.android.zuboradiary.domain.usecase.diary.error

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseError

internal sealed class FetchWeatherInfoError(
    message: String,
    cause: Throwable? = null
) : UseCaseError(message, cause) {

    class LocationPermissionNotGranted(
        cause: Throwable? = null
    ) : FetchWeatherInfoError(
            "位置情報取得権限が未取得です。",
            cause
        )

    class AccessLocation(
        cause: Throwable? = null
    ) : FetchWeatherInfoError(
        "位置情報取得に失敗しました。",
        cause
    )

    class WeatherInfoDateOutOfRange(
        cause: Throwable? = null
    ) : FetchWeatherInfoError(
        "選択した日付は、天気情報を取得できる範囲を超えています。 ",
        cause
    )

    class LoadWeatherInfo(
        cause: Throwable? = null
    ) : FetchWeatherInfoError(
        "天気情報取得に失敗しました。",
        cause
    )
}
