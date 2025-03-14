package com.websarva.wings.android.zuboradiary.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 *
 * Class LocalDate を用途に合わせたの文字列に変換するクラス。
 */
internal class DateTimeStringConverter {

    private val dateFormat = "yyyy年MM月dd日(E)"
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)

    private val dateTimeFormat = "yyyy年MM月dd日(E) HH:mm:ss"
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat)

    private val timeHourMinuteFormat = "HH:mm"
    private val timeHourMinuteFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern(timeHourMinuteFormat)

    fun toYearMonthDayWeek(localDate: LocalDate): String {
        return localDate.format(dateFormatter)
    }

    fun toYearMonthDayWeekHourMinuteSeconds(localDateTime: LocalDateTime): String {
        return localDateTime.format(dateTimeFormatter)
    }

    fun toHourMinute(localTime: LocalTime): String {
        return localTime.format(timeHourMinuteFormatter)
    }
}
