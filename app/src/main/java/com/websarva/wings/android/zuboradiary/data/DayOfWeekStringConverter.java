package com.websarva.wings.android.zuboradiary.data;

import android.content.Context;

import com.websarva.wings.android.zuboradiary.R;

import java.time.DayOfWeek;

/**
 *
 * Enum DayOfWeeK を用途に合わせた文字列に変換するクラス。
 * */
public class DayOfWeekStringConverter {
    private Context context;

    public DayOfWeekStringConverter(Context context) {
        this.context = context;
    }

    public String toCalendarStartDayOfWeek(DayOfWeek dayOfWeek) {
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

    public String toDiaryListDayOfWeek(DayOfWeek dayOfWeek) {
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
