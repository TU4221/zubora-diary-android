package com.websarva.wings.android.zuboradiary.data.mapper.settings

import com.websarva.wings.android.zuboradiary.data.preferences.CalendarStartDayOfWeekPreference
import com.websarva.wings.android.zuboradiary.domain.model.settings.CalendarStartDayOfWeekSetting
import java.time.DayOfWeek

internal fun CalendarStartDayOfWeekPreference.toDomainModel(): CalendarStartDayOfWeekSetting {
    val dayOfWeek = DayOfWeek.of(dayOfWeekNumber)
    return CalendarStartDayOfWeekSetting(dayOfWeek)
}

internal fun CalendarStartDayOfWeekSetting.toDataModel(): CalendarStartDayOfWeekPreference {
    val dayOfWeekNumber = dayOfWeek.value
    return CalendarStartDayOfWeekPreference(dayOfWeekNumber)
}
