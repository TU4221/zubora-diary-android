package com.websarva.wings.android.zuboradiary.ui.fragment.common

import android.content.Context
import android.widget.TextView
import com.websarva.wings.android.zuboradiary.ui.model.diary.WeatherUi
import com.websarva.wings.android.zuboradiary.ui.utils.asString

internal class DiaryWeatherTextUpdater {

    fun update(context: Context, textView: TextView, weather: WeatherUi) {
        textView.text = weather.asString(context)
    }
}
