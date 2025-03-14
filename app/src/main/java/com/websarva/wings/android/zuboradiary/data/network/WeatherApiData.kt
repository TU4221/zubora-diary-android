package com.websarva.wings.android.zuboradiary.data.network

import android.util.Log
import com.squareup.moshi.Json
import com.websarva.wings.android.zuboradiary.data.diary.Weather

// MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
//      その為、@Suppress("RedundantSuppression")で警告回避。
@Suppress("RedundantSuppression")
// MEMO:constructorは直接使用されていないがRetrofit2(Moshi)にてインスタンス化している為、@Suppressで警告回避。
internal data class WeatherApiData @Suppress("unused") constructor(
    // MEMO:フィールド変数はRetrofit2(Moshi)にて代入。
    private val latitude: Float,
    private val longitude: Float,
    private val daily: WeatherApiResponseDairy
) {
    fun toWeatherInfo(): Weather {
        // GeoCoordinatesのコンストラクタを使用してlatitude、longitudeの値チェック
        GeoCoordinates(latitude.toDouble(), longitude.toDouble())

        Log.d("WeatherApi", "latitude:$latitude")
        Log.d("WeatherApi", "longitude:$longitude")
        for (time in daily.times) Log.d("WeatherApi", "time:$time")
        for (code in daily.weatherCodes) Log.d("WeatherApi", "weatherCode:$code")

        val weatherCodes = daily.weatherCodes
        val weatherCode = weatherCodes[0]
        return convertWeathers(weatherCode)
    }

    // "apiWeatherCode"は下記ページの"WMO 気象解釈コード"
    // https://open-meteo.com/en/docs
    private fun convertWeathers(apiWeatherCode: Int): Weather {
        Log.d("WeatherApi", apiWeatherCode.toString())
        return when (apiWeatherCode) {
            0, 1, 2 -> Weather.SUNNY
            3, 45, 48 -> Weather.CLOUDY
            51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82, 95, 96, 99 -> Weather.RAINY
            71, 73, 75, 77, 85, 86 -> Weather.SNOWY
            else -> Weather.UNKNOWN
        }
    }

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
