package com.websarva.wings.android.zuboradiary.domain.model.settings

import java.time.DayOfWeek

internal data class CalendarStartDayOfWeekSetting(
    val dayOfWeek: DayOfWeek
) : UserSetting
