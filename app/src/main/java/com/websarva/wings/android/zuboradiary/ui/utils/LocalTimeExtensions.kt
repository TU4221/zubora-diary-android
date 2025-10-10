package com.websarva.wings.android.zuboradiary.ui.utils

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

internal fun LocalTime.formatHourMinuteString(context: Context): String {
    val timeHourMinuteFormatPattern = context.getString(R.string.local_time_format_pattern_hour_minute)
    val timeHourMinuteFormatter = DateTimeFormatter.ofPattern(timeHourMinuteFormatPattern)
    return this.format(timeHourMinuteFormatter)
}
