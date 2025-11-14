package com.websarva.wings.android.zuboradiary.ui.utils

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import java.time.YearMonth
import java.time.format.DateTimeFormatter

internal fun YearMonth.formatYearMonthString(context: Context): String {
    val dateFormatPattern = context.getString(R.string.year_month_format_pattern_japanese_year_month)
    val dateFormatter = DateTimeFormatter.ofPattern(dateFormatPattern)
    return this.format(dateFormatter)
}
