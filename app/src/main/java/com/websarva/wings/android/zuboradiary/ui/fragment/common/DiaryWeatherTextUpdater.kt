package com.websarva.wings.android.zuboradiary.ui.fragment.common

import android.content.Context
import android.widget.TextView
import com.websarva.wings.android.zuboradiary.domain.model.Weather

internal class DiaryWeatherTextUpdater {

    fun update(context: Context, textView: TextView, weather: Weather) {
        textView.text = weather.toString(context)
    }
}
