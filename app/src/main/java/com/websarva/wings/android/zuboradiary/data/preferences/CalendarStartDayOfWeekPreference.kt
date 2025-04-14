package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import java.time.DayOfWeek


class CalendarStartDayOfWeekPreference {

    // MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
    //      その為、@Suppress("RedundantSuppression")で警告回避。
    @Suppress("unused", "RedundantSuppression") // MEMO:デフォルトパラメータで使用する為、@Suppressで警告回避。
    companion object {
        private val DAY_OF_WEEK_DEFAULT_VALUE = DayOfWeek.SUNDAY
    }

    private val dayOfWeekPreferenceKey = intPreferencesKey("calendar_start_day_of_week")

    private val dayOfWeekNumber: Int

    val dayOfWeek: DayOfWeek
        get() = DayOfWeek.of(dayOfWeekNumber)

    // MEMO:初回読込は"null"が返ってくるので、その場合は初期値を返す。(他のPreferenceValueも同様)
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
