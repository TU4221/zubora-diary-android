package com.websarva.wings.android.zuboradiary.data.network

import java.time.LocalDate

/**
 * 天気情報取得APIによる取得失敗時にスローする例外クラス。
 *
 * 天気情報APIへのアクセスやデータ処理中に何らかの問題が発生した場合に使用する。
 *
 * @param message 例外メッセージ。
 * @param cause 天気情報取得失敗の根本原因となったThrowable (オプショナル)。
 */
internal sealed class WeatherApiException (
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * 天気情報取得APIへのアクセスに失敗した場合の例外。
     *
     * ネットワークエラーやサーバーエラーなど、APIとの通信自体に問題があった場合に使用する。
     *
     * @param cause APIアクセス失敗の根本原因となったThrowable。
     */
    class ApiAccessFailure (
        cause: Throwable
    ) : WeatherApiException("天気情報取得Apiのアクセスに失敗しました。", cause)

    /**
     * 指定された日付が、天気情報を取得できる範囲外である場合の例外。
     *
     * APIがサポートする日付範囲を超えた日付が指定された場合に使用する。
     *
     * @param date 範囲外と判定された日付。
     */
    class DateOutOfRange (
        date: LocalDate
    ) : WeatherApiException("指定した日付 '$date' は、天気情報を取得できる範囲を超えています。")
}
