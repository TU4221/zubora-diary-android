package com.websarva.wings.android.zuboradiary.data;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 *
 * Class LocalDate を用途に合わせたの文字列に変換するクラス。
 * */
public class DateTimeStringConverter {

    private static final String DATE_FORMAT = "yyyy年MM月dd日(E)";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final String DATE_TIME_FORMAT = "yyyy年MM月dd日(E) HH:mm:ss";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    private static final String TIME_HOUR_MINUTE = "HH:mm";
    public static final DateTimeFormatter TIME_HOUR_MINUTE_FORMATTER = DateTimeFormatter.ofPattern(TIME_HOUR_MINUTE);

    @NonNull
    public String toStringDate(LocalDate localDate) {
        Objects.requireNonNull(localDate);

        return localDate.format(DATE_FORMATTER);
        // TODO:下記最終的に削除(下記対応をとらなければ警告があったのに消えた・・・)
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return localDate.format(DATE_FORMATTER);
        } else {
            Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT,Locale.JAPAN);
            return format.format(date);
        }*/
    }

    @NonNull
    public String toStringDateTime(LocalDateTime localDateTime) {
        Objects.requireNonNull(localDateTime);

        return localDateTime.format(DATE_TIME_FORMATTER);
        // TODO:下記最終的に削除(下記対応をとらなければ警告があったのに消えた・・・)
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return localDateTime.format(DATE_TIME_FORMATTER);
        } else {
            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_FORMAT,Locale.JAPAN);
            return format.format(date);
        }*/
    }


    @NonNull
    public String toStringTimeHourMinute(LocalTime localTime) {
        Objects.requireNonNull(localTime);

        return localTime.format(TIME_HOUR_MINUTE_FORMATTER);
    }
}
