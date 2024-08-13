package com.websarva.wings.android.zuboradiary.data;

import android.content.Context;

import com.websarva.wings.android.zuboradiary.R;

import java.time.DayOfWeek;

public class DayOfWeekConverter {
    private Context context;

    public DayOfWeekConverter(Context context) {
        this.context = context;
    }

    public String toStringName(DayOfWeek dayOfWeek) {
        int resId;
        switch (dayOfWeek) {
            case SUNDAY:
                resId = R.string.day_of_week_name_sunday;
                break;
            case MONDAY:
                resId = R.string.day_of_week_name_monday;
                break;
            case TUESDAY:
                resId = R.string.day_of_week_name_tuesday;
                break;
            case WEDNESDAY:
                resId = R.string.day_of_week_name_wednesday;
                break;
            case THURSDAY:
                resId = R.string.day_of_week_name_thursday;
                break;
            case FRIDAY:
                resId = R.string.day_of_week_name_friday;
                break;
            case SATURDAY:
                resId = R.string.day_of_week_name_saturday;
                break;
            default:
                return "";
        }
        return context.getString(resId);
    }

    public String toStringShortName(DayOfWeek dayOfWeek) {
        int resId;
        switch (dayOfWeek) {
            case SUNDAY:
                resId = R.string.day_of_week_short_name_sunday;
                break;
            case MONDAY:
                resId = R.string.day_of_week_short_name_monday;
                break;
            case TUESDAY:
                resId = R.string.day_of_week_short_name_tuesday;
                break;
            case WEDNESDAY:
                resId = R.string.day_of_week_short_name_wednesday;
                break;
            case THURSDAY:
                resId = R.string.day_of_week_short_name_thursday;
                break;
            case FRIDAY:
                resId = R.string.day_of_week_short_name_friday;
                break;
            case SATURDAY:
                resId = R.string.day_of_week_short_name_saturday;
                break;
            default:
                return "";
        }
        return context.getString(resId);
    }
}
