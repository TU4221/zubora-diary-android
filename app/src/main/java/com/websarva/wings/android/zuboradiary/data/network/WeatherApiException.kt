package com.websarva.wings.android.zuboradiary.data.network

import com.websarva.wings.android.zuboradiary.data.model.DataException
import java.time.LocalDate

internal sealed class WeatherApiException (
    message: String,
    cause: Throwable? = null
) : DataException(message, cause) {

    class ApiAccessFailed (
        cause: Throwable? = null
    ) : WeatherApiException("天気情報取得Apiのアクセスに失敗しました。", cause)

    class DateOutOfRange (
        date: LocalDate,
        cause: Throwable? = null
    ) : WeatherApiException("指定した日付 '$date' は、天気情報を取得できる範囲を超えています。", cause)
}
