package com.websarva.wings.android.zuboradiary.data.network

import com.squareup.moshi.Json

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

    // MEMO:constructorは直接使用されていないがRetrofit2(Moshi)にてインスタンス化している為、@Suppressで警告回避。
    data class WeatherApiResponseDairy @Suppress("unused") constructor(
        @Json(name = "time")
        val times: Array<String>,
        @Json(name = "weather_code")
        val weatherCodes: IntArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as WeatherApiResponseDairy

            if (!times.contentEquals(other.times)) return false
            if (!weatherCodes.contentEquals(other.weatherCodes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = times.contentHashCode()
            result = 31 * result + weatherCodes.contentHashCode()
            return result
        }
    }
}
