package com.websarva.wings.android.zuboradiary.data.mapper.weather

import android.util.Log
import com.websarva.wings.android.zuboradiary.data.network.WeatherApiData
import com.websarva.wings.android.zuboradiary.domain.model.diary.Weather
import com.websarva.wings.android.zuboradiary.core.utils.logTag

internal fun WeatherApiData.toDomainModel(): Weather
{
    return toWeatherInfo(this, logTag)
}

private fun toWeatherInfo(date: WeatherApiData, logTag: String): Weather {
    val daily = date.daily
    Log.d(logTag, "toWeatherInfo()_latitude = ${date.latitude}, longitude = ${date.longitude}")
    if (daily.timeList.isNotEmpty()
        && daily.weatherCodeList.isNotEmpty()
        && daily.timeList.size == daily.weatherCodeList.size) {
        for (i in  0 ..< daily.timeList.size) {
            Log.d(
                logTag,
                "toWeatherInfo()_time = " + daily.timeList[i] +
                        ", weatherCode = " + daily.weatherCodeList[i]
            )
        }
    }

    val weatherCodes = daily.weatherCodeList
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
