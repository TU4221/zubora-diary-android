package com.websarva.wings.android.zuboradiary.ui.utils

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

internal fun LocalDate.formatJapaneseDateString(context: Context): String {
    val dateFormatPattern = context.getString(R.string.local_date_format_pattern_japanese_date)
    val dateFormatter = DateTimeFormatter.ofPattern(dateFormatPattern)
    return this.format(dateFormatter)
}

internal fun LocalDateTime.formatJapaneseDateTimeWithSecondsString(context: Context): String {
    val dateTimeFormatPattern = context.getString(R.string.local_date_time_format_pattern_japanese_date_time_with_seconds)
    val dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormatPattern)
    return this.format(dateTimeFormatter)
}

internal fun LocalTime.formatToHourMinuteString(context: Context): String {
    val timeHourMinuteFormatPattern = context.getString(R.string.local_time_format_pattern_hour_minute)
    val timeHourMinuteFormatter = DateTimeFormatter.ofPattern(timeHourMinuteFormatPattern)
    return this.format(timeHourMinuteFormatter)
}
