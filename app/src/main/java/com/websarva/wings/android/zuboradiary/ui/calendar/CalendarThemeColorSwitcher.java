package com.websarva.wings.android.zuboradiary.ui.calendar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.BaseThemeColorSwitcher;

import dagger.internal.Preconditions;

class CalendarThemeColorSwitcher extends BaseThemeColorSwitcher {
    CalendarThemeColorSwitcher(Context context, ThemeColor themeColor) {
        super(context, themeColor);
    }

    void switchCalendarTodayColor(TextView textDay, View viewDot) {
        Preconditions.checkNotNull(textDay);
        Preconditions.checkNotNull(viewDot);

        int color = themeColor.getSecondaryContainerColor(resources);
        int onColor = themeColor.getOnSecondaryContainerColor(resources);
        switchCalendarDayColor(color, onColor, textDay, viewDot);
    }

    void switchCalendarSelectedDayColor(TextView textDay, View viewDot) {
        Preconditions.checkNotNull(textDay);
        Preconditions.checkNotNull(viewDot);

        int color = themeColor.getSecondaryColor(resources);
        int onColor = themeColor.getOnSecondaryColor(resources);
        switchCalendarDayColor(color, onColor, textDay, viewDot);
    }

    void switchCalendarNormalDayColor(TextView textDay, View viewDot) {
        Preconditions.checkNotNull(textDay);
        Preconditions.checkNotNull(viewDot);

        int color = themeColor.getSurfaceColor(resources);
        int onColor = themeColor.getOnSurfaceColor(resources);
        switchCalendarDayColor(color, onColor, textDay, viewDot);
    }

    private void switchCalendarDayColor(int color, int onColor, TextView textDay, View viewDot) {
        Preconditions.checkNotNull(textDay);
        Preconditions.checkNotNull(viewDot);

        Drawable drawable =
                ResourcesCompat.getDrawable(resources, R.drawable.bg_calendar_day, null);

        switchDrawableColor(drawable, color);
        textDay.setBackground(drawable);

        switchTextViewColorOnlyText(textDay, onColor);
        switchViewColor(viewDot, onColor);
    }

    void switchCalendarDayOfWeekColor(TextView textDayOfWeek) {
        Preconditions.checkNotNull(textDayOfWeek);

        int onColor = themeColor.getOnSurfaceColor(resources);
        switchTextViewColorOnlyText(textDayOfWeek, onColor);
    }
}
