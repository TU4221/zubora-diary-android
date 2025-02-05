package com.websarva.wings.android.zuboradiary.data.diary

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import java.util.Arrays

enum class Weather(private val number: Int, private val stringResId: Int) {
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
