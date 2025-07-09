package com.websarva.wings.android.zuboradiary.data.preferences

import java.time.DayOfWeek


internal data class CalendarStartDayOfWeekPreference(
    val dayOfWeekNumber: Int = DayOfWeek.SUNDAY.value
) : UserPreference
