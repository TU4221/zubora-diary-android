package com.websarva.wings.android.zuboradiary.ui.adapter.spinner

import android.content.Context
import android.widget.ArrayAdapter
import androidx.appcompat.view.ContextThemeWrapper
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.WeatherUi
import com.websarva.wings.android.zuboradiary.ui.mapper.asString
import com.websarva.wings.android.zuboradiary.ui.mapper.themeResId

internal class WeatherSpinnerAdapter(
    context: Context,
    themeColor: ThemeColorUi,
    vararg ignoreWeathers: WeatherUi
) : ArrayAdapter<String>(
    ContextThemeWrapper(context, themeColor.themeResId),
    R.layout.layout_drop_down_list_item,
    WeatherUi.entries.toList().filter { it !in ignoreWeathers }.map { it.asString(context) }
) {

    /**
     * 指定された位置に対応する [WeatherUi] を返す。
     *
     * @param position 取得したいアイテムの位置。
     * @return 指定された位置にある [WeatherUi] 。
     * @throws IndexOutOfBoundsException 指定された位置が不正な場合にスローされる。
     */
    fun getWeatherUiItem(position: Int): WeatherUi {
        return WeatherUi.entries[position]
    }
}
