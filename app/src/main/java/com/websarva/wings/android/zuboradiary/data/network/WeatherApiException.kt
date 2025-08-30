package com.websarva.wings.android.zuboradiary.data.network

import java.time.LocalDate


internal sealed class WeatherApiException (
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class ApiAccessFailure (
        cause: Throwable
    ) : WeatherApiException("天気情報取得Apiのアクセスに失敗しました。", cause)

    class DateOutOfRange (
        date: LocalDate
    ) : WeatherApiException("指定した日付 '$date' は、天気情報を取得できる範囲を超えています。")
}
