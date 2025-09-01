package com.websarva.wings.android.zuboradiary.ui.mapper

import com.websarva.wings.android.zuboradiary.domain.model.Weather
import com.websarva.wings.android.zuboradiary.ui.model.WeatherUi

internal fun Weather.toUiModel(): WeatherUi {
    return when (this) {
        Weather.UNKNOWN -> WeatherUi.UNKNOWN
        Weather.SUNNY ->  WeatherUi.SUNNY
        Weather.CLOUDY -> WeatherUi.CLOUDY
        Weather.RAINY -> WeatherUi.RAINY
        Weather.SNOWY -> WeatherUi.SNOWY
    }
}

internal fun WeatherUi.toDomainModel(): Weather {
    return when (this) {
        WeatherUi.UNKNOWN -> Weather.UNKNOWN
        WeatherUi.SUNNY ->  Weather.SUNNY
        WeatherUi.CLOUDY -> Weather.CLOUDY
        WeatherUi.RAINY -> Weather.RAINY
        WeatherUi.SNOWY -> Weather.SNOWY
    }
}
