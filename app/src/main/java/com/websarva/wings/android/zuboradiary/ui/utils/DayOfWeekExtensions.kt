package com.websarva.wings.android.zuboradiary.ui.utils

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import java.time.DayOfWeek

internal fun DayOfWeek.asCalendarStartDayOfWeekString(context: Context): String {
    val resId = when (this) {
        DayOfWeek.SUNDAY -> R.string.day_of_week_name_sunday
        DayOfWeek.MONDAY -> R.string.day_of_week_name_monday
        DayOfWeek.TUESDAY -> R.string.day_of_week_name_tuesday
        DayOfWeek.WEDNESDAY -> R.string.day_of_week_name_wednesday
        DayOfWeek.THURSDAY -> R.string.day_of_week_name_thursday
        DayOfWeek.FRIDAY -> R.string.day_of_week_name_friday
        DayOfWeek.SATURDAY -> R.string.day_of_week_name_saturday
    }
    return context.getString(resId)
}

internal fun DayOfWeek.asDiaryListDayOfWeekString(context: Context): String {
    val resId = when (this) {
        DayOfWeek.SUNDAY -> R.string.day_of_week_short_name_sunday
        DayOfWeek.MONDAY -> R.string.day_of_week_short_name_monday
        DayOfWeek.TUESDAY -> R.string.day_of_week_short_name_tuesday
        DayOfWeek.WEDNESDAY -> R.string.day_of_week_short_name_wednesday
        DayOfWeek.THURSDAY -> R.string.day_of_week_short_name_thursday
        DayOfWeek.FRIDAY -> R.string.day_of_week_short_name_friday
        DayOfWeek.SATURDAY -> R.string.day_of_week_short_name_saturday
    }
    return context.getString(resId)
}
