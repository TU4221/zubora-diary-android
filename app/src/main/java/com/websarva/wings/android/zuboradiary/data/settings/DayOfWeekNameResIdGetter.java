package com.websarva.wings.android.zuboradiary.data.settings;

import com.websarva.wings.android.zuboradiary.R;

import java.time.DayOfWeek;

public class DayOfWeekNameResIdGetter {
    public Integer getResId(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case SUNDAY:
                return R.string.dialog_fragment_number_picker_sunday;
            case MONDAY:
                return R.string.dialog_fragment_number_picker_monday;
            case TUESDAY:
                return R.string.dialog_fragment_number_picker_tuesday;
            case WEDNESDAY:
                return R.string.dialog_fragment_number_picker_wednesday;
            case THURSDAY:
                return R.string.dialog_fragment_number_picker_thursday;
            case FRIDAY:
                return R.string.dialog_fragment_number_picker_friday;
            case SATURDAY:
                return R.string.dialog_fragment_number_picker_saturday;
            default:
                return null;
        }
    }
}
