package com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo.FetchWeatherInfoUseCase
import java.time.LocalDate

/**
 * [FetchWeatherInfoUseCase]の処理中に発生しうる、より具体的な例外を示すシールドクラス。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]、または `null`。
 */
internal sealed class WeatherInfoFetchException(
    message: String,
    cause: Throwable? = null
) : UseCaseException(message, cause) {

    /**
     * 位置情報の取得権限が付与されていない場合の例外。
     */
    class LocationPermissionNotGranted : WeatherInfoFetchException("位置情報取得権限が未取得です。")

    /**
     * 位置情報の取得に失敗した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class LocationAccessFailure(
        cause: Throwable
    ) : WeatherInfoFetchException(
        "位置情報取得に失敗しました。",
        cause
    )

    /**
     * 指定された日付が、天気情報を取得できる範囲外である場合の例外。
     *
     * @param date 範囲外と判定された日付。
     * @param cause 発生した根本的な原因となった [Throwable]、または `null`。
     *              ドメイン層で特定の状況を表現するために直接この例外がインスタンス化され、
     *              ラップする下位層の例外が存在しない場合は `null` となる。
     */
    class DateOutOfRange(
        date: LocalDate,
        cause: Throwable? = null
    ) : WeatherInfoFetchException("指定した日付 '$date' は、天気情報を取得できる範囲を超えています。", cause)

    /**
     * 天気情報の取得に失敗した場合の例外。
     *
     * @param date 天気情報を取得しようとした対象の日付。
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class FetchFailure(
        date: LocalDate,
        cause: Throwable
    ) : WeatherInfoFetchException(
        "日付 '$date' の天気情報の取得に失敗しました。",
        cause
    )
}
