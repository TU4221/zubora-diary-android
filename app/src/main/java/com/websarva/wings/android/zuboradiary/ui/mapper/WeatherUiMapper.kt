package com.websarva.wings.android.zuboradiary.ui.mapper

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.diary.WeatherUi

/**
 * WeatherUi enumに対応する文字列リソースIDを取得する拡張プロパティ。
 */
internal val WeatherUi.stringResId: Int
    get() = when (this) {
        WeatherUi.UNKNOWN -> R.string.enum_weather_unknown
        WeatherUi.SUNNY -> R.string.enum_weather_sunny
        WeatherUi.CLOUDY -> R.string.enum_weather_cloudy
        WeatherUi.RAINY -> R.string.enum_weather_rainy
        WeatherUi.SNOWY -> R.string.enum_weather_snowy
    }

/**
 * WeatherUiをContextを使ってローカライズされた文字列に変換する拡張関数。
 *
 * @param context 文字列リソース解決のためのContext。
 * @return 対応する文字列。
 */
internal fun WeatherUi.asString(context: Context): String {
    return context.getString(this.stringResId)
}
