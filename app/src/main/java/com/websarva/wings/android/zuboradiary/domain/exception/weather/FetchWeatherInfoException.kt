package com.websarva.wings.android.zuboradiary.domain.exception.weather

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import java.time.LocalDate

internal sealed class FetchWeatherInfoException (
    message: String,
    cause: Throwable
) : DomainException(message, cause) {

    class ApiAccessFailed (
        date: LocalDate,
        cause: Throwable
    ) : FetchWeatherInfoException("日付 '$date' の天気情報の取得に失敗しました。", cause)

    class DateOutOfRange (
        date: LocalDate,
        cause: Throwable
    ) : FetchWeatherInfoException("指定した日付 '$date' は、天気情報を取得できる範囲を超えています。", cause)
}
