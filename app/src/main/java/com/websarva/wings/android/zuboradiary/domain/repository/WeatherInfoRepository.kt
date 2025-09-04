package com.websarva.wings.android.zuboradiary.domain.repository

import com.websarva.wings.android.zuboradiary.domain.exception.DomainException
import com.websarva.wings.android.zuboradiary.domain.exception.weather.WeatherInfoFetchException
import com.websarva.wings.android.zuboradiary.domain.model.Weather
import java.time.LocalDate

/**
 * 天気情報の取得処理を抽象化するリポジトリインターフェース。
 *
 * このインターフェースは、特定の日付の天気情報を取得する機能を提供します。
 *
 * 各メソッドは、操作に失敗した場合にドメイン固有の例外 ([DomainException] のサブクラス) をスローする可能性があります。
 */
internal interface WeatherInfoRepository {

    /**
     * 指定された日付の天気情報を取得可能かどうかを判断する。
     *
     * @param date 天気情報を取得したい日付。
     * @return 取得可能であれば `true`、そうでなければ `false`。
     */
    fun canFetchWeatherInfo(date: LocalDate): Boolean

    /**
     * 指定された日付の天気情報を取得する。
     *
     * @param date 天気情報を取得する対象の日付。
     * @return 取得された天気情報。
     * @throws WeatherInfoFetchException 天気情報の取得に失敗した場合。
     */
    suspend fun fetchWeatherInfo(date: LocalDate): Weather
}
