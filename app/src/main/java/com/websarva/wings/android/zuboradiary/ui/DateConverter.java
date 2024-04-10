package com.websarva.wings.android.zuboradiary.ui;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class DateConverter {

    static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E)");
    static DateTimeFormatter dateYearMonthFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");

    public static String toStringLocalDate(long longDate) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate localDate = LocalDate.ofEpochDay(longDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E)");
            return localDate.format(formatter);
        }
        return "";
    }

    public static String toStringLocalDate(int year, int month, int dayOfMonth) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate localDate = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日(E)");
            return localDate.format(formatter);
        }
        return "";
    }

    public static String toStringLocalDate(LocalDate localDate) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return localDate.format(dateFormatter);
        }
        return "";
    }

    public static String toStringLocalDateYearMonth(int year, int month) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate localDate = YearMonth.of(year, month).atDay(1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月");
            return localDate.format(formatter);
        }
        return "";
    }

    public static String toStringLocalDateYearMonth(String date) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate localDate = LocalDate.parse(date, dateFormatter);
            return localDate.format(dateYearMonthFormatter);
        }
        return "";
    }

}
