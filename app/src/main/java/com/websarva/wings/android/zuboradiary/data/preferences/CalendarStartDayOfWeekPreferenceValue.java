package com.websarva.wings.android.zuboradiary.data.preferences;


import androidx.annotation.NonNull;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Objects;

public class CalendarStartDayOfWeekPreferenceValue {

    private final int dayOfWeekNumber;

    public CalendarStartDayOfWeekPreferenceValue(DayOfWeek dayOfWeek) {
        Objects.requireNonNull(dayOfWeek);

        dayOfWeekNumber = dayOfWeek.getValue();
    }

    public CalendarStartDayOfWeekPreferenceValue(int dayOfWeekNumber) {
        boolean contains =
                Arrays.stream(DayOfWeek.values()).anyMatch(x -> x.getValue() == dayOfWeekNumber);
        if (!contains) throw new IllegalArgumentException();

        this.dayOfWeekNumber = dayOfWeekNumber;
    }

    public int getDayOfWeekNumber() {
        return dayOfWeekNumber;
    }

    @NonNull
    public DayOfWeek toDayOfWeek() {
        return DayOfWeek.of(dayOfWeekNumber);
    }
}
