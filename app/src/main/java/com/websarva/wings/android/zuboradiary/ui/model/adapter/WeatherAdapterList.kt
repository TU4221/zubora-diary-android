package com.websarva.wings.android.zuboradiary.ui.model.adapter

import android.content.Context
import com.websarva.wings.android.zuboradiary.data.model.Weather

internal class WeatherAdapterList {

    private val adapterList: List<Weather>

    constructor(vararg ignoreWeathers: Weather) {
        val initialAdapterList = createInitialAdapterList()
        val customAdapterList = ArrayList(initialAdapterList)
        for (weather in ignoreWeathers) {
            customAdapterList.remove(weather)
        }

        adapterList = customAdapterList.toList()
    }

    constructor() {
        adapterList = createInitialAdapterList()
    }

    private fun createInitialAdapterList(): List<Weather> {
        return Weather.entries.toList()
    }

    fun toStringList(context: Context): List<String> {
        val stringList = ArrayList<String>()
        adapterList.forEach { value: Weather ->
            stringList.add(value.toString(context))
        }
        return stringList
    }
}
