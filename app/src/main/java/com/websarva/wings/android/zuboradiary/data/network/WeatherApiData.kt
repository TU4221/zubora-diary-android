package com.websarva.wings.android.zuboradiary.data.network

import com.squareup.moshi.Json

/**
 * 天気情報APIのレスポンスデータを表すデータクラス。
 *
 * このクラスは、Open-Meteo APIからの天気予報データを保持する。
 * 緯度、経度、および日ごとの天気情報（時間と天気コード）を含む。
 *
 * 詳細は [Open-Meteo API Documentation](https://open-meteo.com/en/docs) を参照。
 *
 * @property latitude 取得した天気情報の緯度。
 * @property longitude 取得した天気情報の経度。
 * @property daily 日ごとの天気情報。
 */
// MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
//      その為、@Suppress("RedundantSuppression")で警告回避。
@Suppress("RedundantSuppression")
// MEMO:constructorは直接使用されていないがRetrofit2(Moshi)にてインスタンス化している為、@Suppressで警告回避。
internal data class WeatherApiData @Suppress("unused") constructor(
    // MEMO:フィールド変数はRetrofit2(Moshi)にて代入。
    val latitude: Float,
    val longitude: Float,
    val daily: WeatherApiResponseDairy
) {

    /**
     * 日ごとの天気情報APIレスポンスの内部データを表すデータクラス。
     *
     * 特定の日付に対応する時間ごとの天気コードのリストを保持する。
     *
     * 詳細は [Open-Meteo API Documentation](https://open-meteo.com/en/docs) を参照。
     *
     * @property timeList 天気情報の日付のリスト (YYYY-MM-DD)。
     * @property weatherCodeList 対応する天気コードのリスト 。
     */
    // MEMO:constructorは直接使用されていないがRetrofit2(Moshi)にてインスタンス化している為、@Suppressで警告回避。
    data class WeatherApiResponseDairy @Suppress("unused") constructor(
        @param:Json(name = "time")
        val timeList: List<String>,
        @param:Json(name = "weather_code")
        val weatherCodeList: List<Int>
    )
}
