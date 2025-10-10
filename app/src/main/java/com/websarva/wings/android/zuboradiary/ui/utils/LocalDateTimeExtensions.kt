package com.websarva.wings.android.zuboradiary.ui.utils

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal fun LocalDateTime.formatDateTimeWithSecondsString(context: Context): String {
    val dateTimeFormatPattern = context.getString(R.string.local_date_time_format_pattern_japanese_date_time_with_seconds)
    val dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormatPattern)
    return this.format(dateTimeFormatter)
}
