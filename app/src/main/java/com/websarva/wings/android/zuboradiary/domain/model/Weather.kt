package com.websarva.wings.android.zuboradiary.domain.model

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import java.util.Arrays

// MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
//      その為、@Suppress("RedundantSuppression")で警告回避。
@Suppress("RedundantSuppression")
// MEMO:constructorは直接使用されていないが必要な為、@Suppressで警告回避。
internal enum class Weather @Suppress("unused") constructor(
    private val number: Int,
    private val stringResId: Int
) {

    UNKNOWN(0, R.string.enum_weather_unknown),
    SUNNY(1, R.string.enum_weather_sunny),
    CLOUDY(2, R.string.enum_weather_cloudy),
    RAINY(3, R.string.enum_weather_rainy),
    SNOWY(4, R.string.enum_weather_snowy);

    companion object {
        fun of(number: Int): Weather {
            return Arrays.stream(entries.toTypedArray())
                .filter { x: Weather -> x.toNumber() == number }.findFirst().get()
        }

        fun of(context: Context, strWeather: String): Weather {
            return Arrays.stream(entries.toTypedArray())
                .filter { x: Weather -> x.toString(context) == strWeather }.findFirst().get()
        }
    }

    fun toString(context: Context): String {
        return context.getString(stringResId)
    }

    fun toNumber(): Int {
        return number
    }
}
