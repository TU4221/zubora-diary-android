package com.websarva.wings.android.zuboradiary.data.preferences

import java.time.DayOfWeek


internal class CalendarStartDayOfWeekPreference {

    companion object {
        val DAY_OF_WEEK_DEFAULT_VALUE = DayOfWeek.SUNDAY.value
    }

    val dayOfWeekNumber: Int

    val dayOfWeek: DayOfWeek
        get() = DayOfWeek.of(dayOfWeekNumber)

    constructor(dayOfWeekNumber: Int) {
        DayOfWeek.of(dayOfWeekNumber)

        this.dayOfWeekNumber = dayOfWeekNumber
    }

    constructor(dayOfWeek: DayOfWeek) {
        dayOfWeekNumber = dayOfWeek.value
    }

    constructor(): this(DAY_OF_WEEK_DEFAULT_VALUE)
}
