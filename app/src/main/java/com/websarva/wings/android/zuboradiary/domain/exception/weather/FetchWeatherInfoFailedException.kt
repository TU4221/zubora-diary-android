package com.websarva.wings.android.zuboradiary.domain.exception.weather

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException

internal class FetchWeatherInfoFailedException (
    /*date: LocalDate,*/
    cause: Throwable
    // TODO:天気情報の取得時、ドメイン側では日付の指定で取得できるように修正する。
) : /*DomainException("日付 '$date' の天気情報の取得に失敗しました。", cause)*/
    DomainException("天気情報の取得に失敗しました。", cause)
