package com.websarva.wings.android.zuboradiary.ui.diary.common.utils

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import java.time.DayOfWeek

/**
 *  [DayOfWeek]を、日記リストアイテムの日付欄に表示するための文字列（例: "日"）に変換する。
 * @param context 文字列リソースを取得するためのコンテキスト。
 */
internal fun DayOfWeek.asDiaryListDayOfWeekString(context: Context): String {
    val resId = when (this) {
        DayOfWeek.SUNDAY -> R.string.enum_day_of_week_short_name_sunday
        DayOfWeek.MONDAY -> R.string.enum_day_of_week_short_name_monday
        DayOfWeek.TUESDAY -> R.string.enum_day_of_week_short_name_tuesday
        DayOfWeek.WEDNESDAY -> R.string.enum_day_of_week_short_name_wednesday
        DayOfWeek.THURSDAY -> R.string.enum_day_of_week_short_name_thursday
        DayOfWeek.FRIDAY -> R.string.enum_day_of_week_short_name_friday
        DayOfWeek.SATURDAY -> R.string.enum_day_of_week_short_name_saturday
    }
    return context.getString(resId)
}
