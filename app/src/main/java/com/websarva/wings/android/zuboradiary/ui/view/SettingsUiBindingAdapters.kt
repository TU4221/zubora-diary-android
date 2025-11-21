package com.websarva.wings.android.zuboradiary.ui.view

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.utils.asCalendarStartDayOfWeekString
import com.websarva.wings.android.zuboradiary.ui.utils.asString
import com.websarva.wings.android.zuboradiary.ui.utils.formatHourMinuteString
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * 設定画面に関連するカスタムBinding Adapterを定義するオブジェクト。
 */
internal object SettingsUiBindingAdapters {

    /**
     * `Any?`型の値を、その型に応じた適切な文字列にフォーマットして[TextView]に設定する。
     *
     * [ThemeColorUi]、[DayOfWeek]、[LocalTime]など、複数の型を単一のBinding Adapterで処理するために使用される。
     *
     * @param textView 対象のTextView。
     * @param value フォーマットして表示する値。nullの場合は空文字列が設定される。
     */
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
