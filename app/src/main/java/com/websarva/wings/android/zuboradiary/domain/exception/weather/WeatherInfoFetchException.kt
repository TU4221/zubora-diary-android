package com.websarva.wings.android.zuboradiary.domain.exception.weather

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import java.time.LocalDate

/**
 * 天気情報の取得処理中にエラーが発生した場合にスローされる例外クラス。
 *
 * @param message エラーメッセージ。
 * @param cause 発生した根本的な原因となった[Throwable]。
 */
internal sealed class WeatherInfoFetchException (
    message: String,
    cause: Throwable
) : DomainException(message, cause) {

    /**
     * 位置情報の取得に失敗した場合にスローされる例外。
     *
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class AccessLocationFailure (
        cause: Throwable
    ) : WeatherInfoFetchException("位置情報の取得に失敗しました。", cause)

    /**
     * 天気情報APIへのアクセスに失敗した場合にスローされる例外。
     *
     * @param date 天気情報を取得しようとした対象の日付。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class ApiAccessFailure (
        date: LocalDate,
        cause: Throwable
    ) : WeatherInfoFetchException("日付 '$date' の天気情報の取得に失敗しました。", cause)

    /**
     * 指定された日付が、天気情報を取得できる範囲外である場合にスローされる例外。
     *
     * @param date 範囲外と判定された日付。
     * @param cause 発生した根本的な原因となった[Throwable]。
     */
    class DateOutOfRange (
        date: LocalDate,
        cause: Throwable
    ) : WeatherInfoFetchException("指定した日付 '$date' は、天気情報を取得できる範囲を超えています。", cause)
}
