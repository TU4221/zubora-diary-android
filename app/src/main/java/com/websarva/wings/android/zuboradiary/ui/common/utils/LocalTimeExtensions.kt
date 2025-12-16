package com.websarva.wings.android.zuboradiary.ui.common.utils

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * [LocalTime]を、時間書式（例: 13:05）の文字列に変換する。
 * @param context 書式パターンを取得するためのコンテキスト。
 */
internal fun LocalTime.formatHourMinuteString(context: Context): String {
    val timeHourMinuteFormatPattern = context.getString(R.string.local_time_format_pattern_hour_minute)
    val timeHourMinuteFormatter = DateTimeFormatter.ofPattern(timeHourMinuteFormatPattern)
    return this.format(timeHourMinuteFormatter)
}
