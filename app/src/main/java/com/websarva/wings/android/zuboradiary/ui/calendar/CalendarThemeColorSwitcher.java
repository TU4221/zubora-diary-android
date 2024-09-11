package com.websarva.wings.android.zuboradiary.ui.calendar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.BaseThemeColorSwitcher;
import com.websarva.wings.android.zuboradiary.ui.ColorSwitchingViewList;

import kotlin.TypeCastException;

class CalendarThemeColorSwitcher extends BaseThemeColorSwitcher {
    CalendarThemeColorSwitcher(Context context, ThemeColor themeColor) {
        super(context, themeColor);
    }

    void switchCalendarTodayColor(TextView textDay, View viewDot) {
        if (textDay == null) {
            throw new NullPointerException();
        }
        if (viewDot == null) {
            throw new NullPointerException();
        }

        int secondaryContainerColor = themeColor.getSecondaryContainerColor(resources);
        int onSecondaryContainerColor = themeColor.getOnSecondaryContainerColor(resources);
        switchCalendarDayColor(secondaryContainerColor, onSecondaryContainerColor, textDay, viewDot);
    }

    void switchCalendarSelectedDayColor(TextView textDay, View viewDot) {
        if (textDay == null) {
            throw new NullPointerException();
        }
        if (viewDot == null) {
            throw new NullPointerException();
        }

        int secondaryColor = themeColor.getSecondaryColor(resources);
        int onSecondaryColor = themeColor.getOnSecondaryColor(resources);
        switchCalendarDayColor(secondaryColor, onSecondaryColor, textDay, viewDot);
    }

    void switchCalendarNormalDayColor(TextView textDay, View viewDot) {
        if (textDay == null) {
            throw new NullPointerException();
        }
        if (viewDot == null) {
            throw new NullPointerException();
        }

        int surfaceColor = themeColor.getSurfaceColor(resources);
        int onSurfaceColor = themeColor.getOnSurfaceColor(resources);
        switchCalendarDayColor(surfaceColor, onSurfaceColor, textDay, viewDot);
    }

    private void switchCalendarDayColor(int color, int onColor, TextView textDay, View viewDot) {
        Drawable drawable =
                ResourcesCompat.getDrawable(
                        context.getResources(), R.drawable.bg_calendar_day, null);
        if (drawable == null) {
            throw new NullPointerException();
        }
        if (drawable instanceof GradientDrawable) {
            GradientDrawable gradientDrawable = (GradientDrawable) drawable;
            gradientDrawable.setColor(color);
            textDay.setBackground(gradientDrawable);
        } else {
            throw new TypeCastException();
        }

        textDay.setTextColor(onColor);
        viewDot.setBackgroundColor(onColor);
    }

    void switchCalendarDayOfWeekColor(TextView textDayOfWeek) {
        if (textDayOfWeek == null) {
            throw new NullPointerException();
        }

        int onSurfaceColor = themeColor.getOnSurfaceColor(resources);
        ColorSwitchingViewList<TextView> textDayOfWeekList = new ColorSwitchingViewList<>(textDayOfWeek);
        switchTextViewColorOnlyText(onSurfaceColor, textDayOfWeekList);
    }
}
