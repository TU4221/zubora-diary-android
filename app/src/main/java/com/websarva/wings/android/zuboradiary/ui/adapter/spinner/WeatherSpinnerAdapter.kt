package com.websarva.wings.android.zuboradiary.ui.adapter.spinner

import android.content.Context
import android.widget.ArrayAdapter
import androidx.appcompat.view.ContextThemeWrapper
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.model.WeatherUi

internal class WeatherSpinnerAdapter(
    context: Context,
    themeColor: ThemeColor,
    vararg ignoreWeathers: WeatherUi
) : ArrayAdapter<String>(
    ContextThemeWrapper(context, themeColor.themeResId),
    R.layout.layout_drop_down_list_item,
    WeatherUi.entries.toList().filter { it !in ignoreWeathers }.map { it.toString(context) }
)
