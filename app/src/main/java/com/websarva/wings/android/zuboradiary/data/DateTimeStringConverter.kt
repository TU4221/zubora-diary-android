package com.websarva.wings.android.zuboradiary.data;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 *
 * Class LocalDate を用途に合わせたの文字列に変換するクラス。
 * */
public class DateTimeStringConverter {

    private final String DATE_FORMAT = "yyyy年MM月dd日(E)";
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private final String DATE_TIME_FORMAT = "yyyy年MM月dd日(E) HH:mm:ss";
    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    private final String TIME_HOUR_MINUTE = "HH:mm";
    private final DateTimeFormatter TIME_HOUR_MINUTE_FORMATTER = DateTimeFormatter.ofPattern(TIME_HOUR_MINUTE);

    @NonNull
    public String toYearMonthDayWeek(LocalDate localDate) {
        Objects.requireNonNull(localDate);

        return localDate.format(DATE_FORMATTER);
    }

    @NonNull
    public String toYearMonthDayWeekHourMinuteSeconds(LocalDateTime localDateTime) {
        Objects.requireNonNull(localDateTime);

        return localDateTime.format(DATE_TIME_FORMATTER);
    }


    @NonNull
    public String toHourMinute(LocalTime localTime) {
        Objects.requireNonNull(localTime);

        return localTime.format(TIME_HOUR_MINUTE_FORMATTER);
    }
}
