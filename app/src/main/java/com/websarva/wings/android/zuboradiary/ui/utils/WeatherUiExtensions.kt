package com.websarva.wings.android.zuboradiary.ui.utils

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.diary.WeatherUi

/** [WeatherUi]に対応する文字列リソースIDを返す。 */
private val WeatherUi.stringResId: Int
    get() = when (this) {
        WeatherUi.UNKNOWN -> R.string.enum_weather_unknown
        WeatherUi.SUNNY -> R.string.enum_weather_sunny
        WeatherUi.CLOUDY -> R.string.enum_weather_cloudy
        WeatherUi.RAINY -> R.string.enum_weather_rainy
        WeatherUi.SNOWY -> R.string.enum_weather_snowy
    }

/**
 * [WeatherUi]を、ユーザーに表示するための文字列に変換する。
 * @param context 文字列リソースを取得するためのコンテキスト。
 */
internal fun WeatherUi.asString(context: Context): String {
    return context.getString(this.stringResId)
}
