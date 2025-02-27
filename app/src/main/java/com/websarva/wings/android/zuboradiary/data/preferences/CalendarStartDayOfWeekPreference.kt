package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import java.time.DayOfWeek


class CalendarStartDayOfWeekPreference {

    @Suppress("unused") // MEMO:デフォルトパラメータで使用する為、@Suppressで警告回避。
    companion object {
        private val DAY_OF_WEEK_DEFAULT_VALUE = DayOfWeek.SUNDAY
    }

    private val dayOfWeekPreferenceKey = intPreferencesKey("calendar_start_day_of_week")

    private val dayOfWeekNumber: Int

    val dayOfWeek: DayOfWeek
        get() = DayOfWeek.of(dayOfWeekNumber)

    constructor(preferences: Preferences) {
        this.dayOfWeekNumber =
            preferences[dayOfWeekPreferenceKey] ?: DAY_OF_WEEK_DEFAULT_VALUE.value
    }

    @JvmOverloads
    constructor(dayOfWeek: DayOfWeek = DAY_OF_WEEK_DEFAULT_VALUE) {
        dayOfWeekNumber = dayOfWeek.value
    }

    fun setUpPreferences(mutablePreferences: MutablePreferences) {
        mutablePreferences[dayOfWeekPreferenceKey] = dayOfWeekNumber
    }
}
