package com.websarva.wings.android.zuboradiary.ui.utils

import android.content.Context
import com.websarva.wings.android.zuboradiary.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * [LocalDateTime]を、日本語の日時書式（例: 2023年10月26日 13:05:01）の文字列に変換する。
 * @param context 書式パターンを取得するためのコンテキスト。
 */
internal fun LocalDateTime.formatDateTimeWithSecondsString(context: Context): String {
    val dateTimeFormatPattern = context.getString(R.string.local_date_time_format_pattern_japanese_date_time_with_seconds)
    val dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormatPattern)
    return this.format(dateTimeFormatter)
}
