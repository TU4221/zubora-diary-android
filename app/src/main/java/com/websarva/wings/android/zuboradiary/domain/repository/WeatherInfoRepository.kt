package com.websarva.wings.android.zuboradiary.domain.repository

import com.websarva.wings.android.zuboradiary.domain.model.location.SimpleLocation
import com.websarva.wings.android.zuboradiary.domain.usecase.UseCaseException
import com.websarva.wings.android.zuboradiary.domain.model.diary.Weather
import com.websarva.wings.android.zuboradiary.domain.exception.InvalidParameterException
import com.websarva.wings.android.zuboradiary.domain.exception.NetworkConnectionException
import java.time.LocalDate

/**
 * 天気情報の取得処理を抽象化するリポジトリインターフェース。
 *
 * このインターフェースは、特定の日付の天気情報を取得する機能を提供します。
 *
 * 各メソッドは、操作に失敗した場合にドメイン固有の例外 ([UseCaseException] のサブクラス) をスローする可能性があります。
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
     * 指定された日付と位置の天気情報を取得する。
     *
     * @param date 天気情報を取得する対象の日付。
     * @param location 天気情報を取得する対象の位置情報。
     * @return 取得された天気情報。
     * @throws NetworkConnectionException 天気情報の取得に失敗した場合。
     * @throws InvalidParameterException 指定された日付が天気情報の取得可能範囲を超えてた場合。
     */
    suspend fun fetchWeatherInfo(date: LocalDate, location: SimpleLocation): Weather
}
