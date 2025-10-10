package com.websarva.wings.android.zuboradiary.ui.utils

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal fun LocalDate.formatDateString(context: Context): String {
    val dateFormatPattern = context.getString(R.string.local_date_format_pattern_japanese_date)
    val dateFormatter = DateTimeFormatter.ofPattern(dateFormatPattern)
    return this.format(dateFormatter)
}
