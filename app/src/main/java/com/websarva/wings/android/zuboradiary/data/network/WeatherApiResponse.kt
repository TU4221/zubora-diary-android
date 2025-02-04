package com.websarva.wings.android.zuboradiary.data.network

import android.util.Log
import com.squareup.moshi.Json
import com.websarva.wings.android.zuboradiary.data.diary.Weather

// HACK:Retrofit2(Moshi)を使用して本クラスをインスタンス化する時、引数ありのコンストラクタは処理されない。
//      引数なしコンストラクタ処理時は、フィールド変数の値は未格納。
//      原因が明らかになるまで、フィールド変数参照時はNullチェック等を行う。
class WeatherApiResponse private constructor() {
    // MEMO:フィールド変数はRetrofit2(Moshi)にて代入。
    private var latitude = 0f
    private var longitude = 0f
    private lateinit var daily: WeatherApiResponseDairy

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

    private class WeatherApiResponseDairy {
        // MEMO:フィールド変数はRetrofit2(Moshi)にて代入。
        @Json(name = "time")
        lateinit var times: Array<String>

        @Json(name = "weather_code")
        lateinit var weatherCodes: IntArray
    }
}
