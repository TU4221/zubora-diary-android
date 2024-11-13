package com.websarva.wings.android.zuboradiary.data.preferences;


import androidx.annotation.NonNull;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Objects;

public class CalendarStartDayOfWeekPreferenceValue {

    static final Preferences.Key<Integer> PREFERENCES_KEY_DAY_OF_WEEK =
            PreferencesKeys.intKey("calendar_start_day_of_week");
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

    public CalendarStartDayOfWeekPreferenceValue() {
        this(DayOfWeek.SUNDAY);
    }

    void setUpPreferences(MutablePreferences mutablePreferences) {
        Objects.requireNonNull(mutablePreferences);

        mutablePreferences.set(PREFERENCES_KEY_DAY_OF_WEEK, dayOfWeekNumber);
    }

    public int getDayOfWeekNumber() {
        return dayOfWeekNumber;
    }

    @NonNull
    public DayOfWeek toDayOfWeek() {
        return DayOfWeek.of(dayOfWeekNumber);
    }
}
