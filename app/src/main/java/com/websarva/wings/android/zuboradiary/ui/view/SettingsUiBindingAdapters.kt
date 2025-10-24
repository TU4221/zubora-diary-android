package com.websarva.wings.android.zuboradiary.ui.view

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.utils.asCalendarStartDayOfWeekString
import com.websarva.wings.android.zuboradiary.ui.utils.asString
import com.websarva.wings.android.zuboradiary.ui.utils.formatHourMinuteString
import java.time.DayOfWeek
import java.time.LocalTime

internal object SettingsUiBindingAdapters {

    @JvmStatic
    @BindingAdapter("settingValueText")
    fun setSettingValueText(textView: TextView, value: Any?) {
        val context = textView.context
        val valueText =
            when (value) {
                is ThemeColorUi -> value.asString(context)
                is DayOfWeek -> value.asCalendarStartDayOfWeekString(context)
                is LocalTime -> value.formatHourMinuteString(context)
                null -> ""
                else -> value.toString()
            }
        if (textView.text.toString() != valueText) {
            textView.text = valueText
        }
    }
}
