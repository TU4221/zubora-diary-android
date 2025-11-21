package com.websarva.wings.android.zuboradiary.ui.utils

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * [YearMonth]を、日本語の年月書式（例: 2023年10月）の文字列に変換する。
 * @param context 書式パターンを取得するためのコンテキスト。
 */
internal fun YearMonth.formatYearMonthString(context: Context): String {
    val dateFormatPattern = context.getString(R.string.year_month_format_pattern_japanese_year_month)
    val dateFormatter = DateTimeFormatter.ofPattern(dateFormatPattern)
    return this.format(dateFormatter)
}
