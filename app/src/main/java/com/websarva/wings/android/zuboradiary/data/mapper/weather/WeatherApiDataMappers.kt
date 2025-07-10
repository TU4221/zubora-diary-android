package com.websarva.wings.android.zuboradiary.data.mapper.weather

import android.util.Log
import com.websarva.wings.android.zuboradiary.domain.model.Weather
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiData
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal fun WeatherApiData.toDomainModel(): Weather {
    return toWeatherInfo(this, createLogTag())
}

private fun toWeatherInfo(date: WeatherApiData, logTag: String): Weather {
    val daily = date.daily
    Log.d(logTag, "toWeatherInfo()_latitude = ${date.latitude}, longitude = ${date.longitude}")
    if (daily.times.isNotEmpty()
        && daily.weatherCodes.isNotEmpty()
        && daily.times.size == daily.weatherCodes.size) {
        for (i in  0 ..< daily.times.size) {
            Log.d(
                logTag,
                "toWeatherInfo()_time = " + daily.times[i] +
                        ", weatherCode = " + daily.weatherCodes[i]
            )
        }
    }

    val weatherCodes = daily.weatherCodes
    val weatherCode = weatherCodes[0]
    return convertWeathers(weatherCode, logTag)
}

// "apiWeatherCode"は下記ページの"WMO 気象解釈コード"
// https://open-meteo.com/en/docs
private fun convertWeathers(apiWeatherCode: Int, logTag: String): Weather {
    val result =
        when (apiWeatherCode) {
            0, 1, 2 -> Weather.SUNNY
            3, 45, 48 -> Weather.CLOUDY
            51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82, 95, 96, 99 -> Weather.RAINY
            71, 73, 75, 77, 85, 86 -> Weather.SNOWY
            else -> Weather.UNKNOWN
        }

    Log.d(logTag, "convertWeathers(apiWeatherCode = $apiWeatherCode) = $result")
    return result
}
