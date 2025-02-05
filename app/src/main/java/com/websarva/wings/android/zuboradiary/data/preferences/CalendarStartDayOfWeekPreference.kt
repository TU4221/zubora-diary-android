package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import java.time.DayOfWeek
import java.util.Arrays


class CalendarStartDayOfWeekPreference {

    companion object {
        @JvmField
        val PREFERENCES_KEY_DAY_OF_WEEK: Preferences.Key<Int> =
            intPreferencesKey("calendar_start_day_of_week")
    }

    private val dayOfWeekNumber: Int

    @JvmOverloads
    constructor(dayOfWeek: DayOfWeek = DayOfWeek.SUNDAY) {
        dayOfWeekNumber = dayOfWeek.value
    }

    constructor(dayOfWeekNumber: Int) {
        val contains =
            Arrays.stream(DayOfWeek.entries.toTypedArray())
                .anyMatch { x: DayOfWeek -> x.value == dayOfWeekNumber }
        require(contains)

        this.dayOfWeekNumber = dayOfWeekNumber
    }

    fun setUpPreferences(mutablePreferences: MutablePreferences) {
        mutablePreferences[PREFERENCES_KEY_DAY_OF_WEEK] = dayOfWeekNumber
    }

    fun toDayOfWeek(): DayOfWeek {
        return DayOfWeek.of(dayOfWeekNumber)
    }
}
