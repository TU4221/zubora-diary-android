package com.websarva.wings.android.zuboradiary.data;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class DateConverter {

    private static final String DATE_FORMAT = "yyyy年MM月dd日(E)";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy年MM月dd日(E) HH:mm:ss");
    public static final DateTimeFormatter DATE_YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月");

    public static final DateTimeFormatter TIME_HOUR_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final int MAX_YEAR = LocalDate.MAX.getYear();
    public static final int MIN_YEAR = LocalDate.MIN.getYear();
    public static final int MAX_MONTH = LocalDate.MAX.getMonthValue();
    public static final int MIN_MONTH = LocalDate.MIN.getMonthValue();
    public static final int MAX_DAY_OF_MONTH = LocalDate.MAX.getDayOfMonth();
    public static final int MIN_DAY_OF_MONTH = LocalDate.MIN.getDayOfMonth();

    public static String toStringLocalDate(int year, int month, int dayOfMonth) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                LocalDate localDate = LocalDate.of(year, month, dayOfMonth);
                return toStringLocalDate(localDate);
            } catch (DateTimeException e) {
                return toStringLocalDateNow();
            }
        }
        return "";
    }

    public static String toStringLocalDate(LocalDate localDate) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return localDate.format(DATE_FORMATTER);
        }
        return "";
    }

    public static String toStringLocalDateNow() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate localDate = LocalDate.now();
            return localDate.format(DATE_FORMATTER);
        }
        return "";
    }

    public static String toStringLocalDateTime(LocalDateTime localDateTime) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return localDateTime.format(DATE_TIME_FORMATTER);
        }
        return "";
    }

    public static String toStringLocalDateTimeNow() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime localDateTime = LocalDateTime.now();
            return localDateTime.format(DATE_TIME_FORMATTER);
        }
        return "";
    }

    public static String toStringLocalDateYearMonth(YearMonth yearMonth) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate localDate =
                    YearMonth.of(yearMonth.getYear(), yearMonth.getMonthValue()).atDay(1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月");
            return localDate.format(formatter);
        }
        return "";
    }

    // TODO:now()ではなく例外を投げる
    public static LocalDate toLocalDate(@NonNull String date) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return LocalDate.parse(date, DATE_FORMATTER);
        }
        return LocalDate.now();
    }

    public static LocalDateTime toLocalDateTime(@NonNull String dateTime) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
        }
        return LocalDateTime.now();
    }

    public static String toStringTimeHourMinute(int hour, int minute) {
        LocalTime localTime = LocalTime.of(hour, minute);
        return  localTime.format(TIME_HOUR_MINUTE_FORMATTER);
    }

    public static boolean isFormatStringDate(String date) {
        try {
            LocalDate.parse(date, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isFormatStringDateTime(String dateTime) {
        try {
            LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
