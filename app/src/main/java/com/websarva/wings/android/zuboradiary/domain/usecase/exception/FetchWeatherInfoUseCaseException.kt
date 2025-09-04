package com.websarva.wings.android.zuboradiary.domain.usecase.exception

import com.websarva.wings.android.zuboradiary.domain.usecase.weatherinfo.FetchWeatherInfoUseCase
import java.time.LocalDate

/**
 * [FetchWeatherInfoUseCase] の実行中に発生しうる、より具体的な失敗原因を示す例外の基底クラス。
 *
 * このクラスの各サブクラスは、天気情報取得処理における異なる失敗シナリオを表します。
 * これにより、ユースケースの呼び出し元は、発生した例外の種類に応じて、
 * より詳細なエラーハンドリングやユーザーへのフィードバックを行うことが可能になります。
 *
 * @param message 例外メッセージ。
 * @param cause この例外を引き起こした根本的な原因となった [Throwable]、または `null`。
 *              ドメイン層で特定の状況を表現するために直接この例外がインスタンス化され、
 *              ラップする下位層の例外が存在しない場合は `null` となる。
 */
internal sealed class FetchWeatherInfoUseCaseException(
    message: String,
    cause: Throwable? = null
) : UseCaseException(message, cause) {

    /**
     * 位置情報の取得権限が付与されていない場合の例外。
     */
    class LocationPermissionNotGranted : FetchWeatherInfoUseCaseException("位置情報取得権限が未取得です。")

    /**
     * 位置情報の取得に失敗した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class LocationAccessFailure(
        cause: Throwable
    ) : FetchWeatherInfoUseCaseException(
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
    class WeatherInfoDateOutOfRange(
        date: LocalDate,
        cause: Throwable? = null
    ) : FetchWeatherInfoUseCaseException("指定した日付 '$date' は、天気情報を取得できる範囲を超えています。", cause)

    /**
     * 天気情報の取得に失敗した場合の例外。
     *
     * @param cause 発生した根本的な原因となった [Throwable]。
     */
    class WeatherInfoFetchFailure(
        cause: Throwable
    ) : FetchWeatherInfoUseCaseException(
        "天気情報取得に失敗しました。",
        cause
    )
}
