package com.websarva.wings.android.zuboradiary.data.exception

internal sealed class FetchWeatherInfoException(
    message: String,
    cause: Throwable? = null
) : UseCaseException(message, cause) {

    class LocationPermissionException(
        cause: Throwable? = null
    ) : FetchWeatherInfoException(
            "位置情報取得権限が未取得です。",
            cause
        )

    class LocationAccessFailedException(
        cause: Throwable? = null
    ) : FetchWeatherInfoException(
        "位置情報取得に失敗しました。",
        cause
    )

    class WeatherInfoDateRangeCheckFailedException(
        cause: Throwable? = null
    ) : FetchWeatherInfoException(
        "選択された日付が天気情報の取得可能範囲内かどうかの確認に失敗しました。 ",
        cause
    )

    class WeatherInfoDateOutOfRangeException(
        cause: Throwable? = null
    ) : FetchWeatherInfoException(
        "選択した日付は、天気情報を取得できる範囲を超えています。 ",
        cause
    )

    class WeatherInfoFetchFailedException(
        cause: Throwable? = null
    ) : FetchWeatherInfoException(
        "位置情報取得に失敗しました。",
        cause
    )
}
