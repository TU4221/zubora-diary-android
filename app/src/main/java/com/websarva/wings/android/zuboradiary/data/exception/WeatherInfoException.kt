package com.websarva.wings.android.zuboradiary.data.exception

internal open class WeatherInfoFetchingException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

internal class LocationPermissionException(cause: Throwable? = null) :
    WeatherInfoFetchingException(
        "位置情報取得権限が未取得です。",
        cause
    )

internal class LocationAccessFailedException(cause: Throwable? = null) :
    WeatherInfoFetchingException(
        "位置情報取得に失敗しました。",
        cause
    )

internal class WeatherInfoDateRangeCheckFailedException(cause: Throwable? = null) :
    WeatherInfoFetchingException(
        "選択された日付が天気情報の取得可能範囲内かどうかの確認に失敗しました。 ",
        cause
    )

internal class WeatherInfoDateOutOfRangeException(cause: Throwable? = null) :
    WeatherInfoFetchingException(
        "選択した日付は、天気情報を取得できる範囲を超えています。 ",
        cause
    )

internal class WeatherInfoFetchFailedException(cause: Throwable? = null) :
    WeatherInfoFetchingException(
        "位置情報取得に失敗しました。",
        cause
    )
