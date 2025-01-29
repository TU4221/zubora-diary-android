package com.websarva.wings.android.zuboradiary.ui.calendar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.ThemeColorSwitcher;

import java.util.Objects;

class CalendarThemeColorSwitcher extends ThemeColorSwitcher {

    CalendarThemeColorSwitcher(Context context, ThemeColor themeColor) {
        super(context, themeColor);
    }

    void switchCalendarTodayColor(TextView textDay, View viewDot) {
        Objects.requireNonNull(textDay);
        Objects.requireNonNull(viewDot);

        int color = getThemeColor().getSecondaryContainerColor(getResources());
        int onColor = getThemeColor().getOnSecondaryContainerColor(getResources());
        switchCalendarDayColor(color, onColor, textDay, viewDot);
    }

    void switchCalendarSelectedDayColor(TextView textDay, View viewDot) {
        Objects.requireNonNull(textDay);
        Objects.requireNonNull(viewDot);

        int color = getThemeColor().getSecondaryColor(getResources());
        int onColor = getThemeColor().getOnSecondaryColor(getResources());
        switchCalendarDayColor(color, onColor, textDay, viewDot);
    }

    void switchCalendarWeekdaysColor(TextView textDay, View viewDot) {
        Objects.requireNonNull(textDay);
        Objects.requireNonNull(viewDot);

        int color = getCalendarSurfaceColor();
        int onColor = getOnWeekdaysColor();
        switchCalendarDayColor(color, onColor, textDay, viewDot);
    }

    void switchCalendarSaturdayColor(TextView textDay, View viewDot) {
        Objects.requireNonNull(textDay);
        Objects.requireNonNull(viewDot);

        int color = getCalendarSurfaceColor();
        int onColor = getOnSaturdayColor();
        switchCalendarDayColor(color, onColor, textDay, viewDot);
    }

    void switchCalendarSundayColor(TextView textDay, View viewDot) {
        Objects.requireNonNull(textDay);
        Objects.requireNonNull(viewDot);

        int color = getCalendarSurfaceColor();
        int onColor = getOnSundayColor();
        switchCalendarDayColor(color, onColor, textDay, viewDot);
    }

    private void switchCalendarDayColor(int color, int onColor, TextView textDay, View viewDot) {
        Objects.requireNonNull(textDay);
        Objects.requireNonNull(viewDot);

        Drawable drawable =
                ResourcesCompat.getDrawable(getResources(), R.drawable.bg_calendar_day, null);

        switchDrawableColor(drawable, color);
        textDay.setBackground(drawable);

        switchTextViewColorOnlyText(textDay, onColor);
        switchViewColor(viewDot, onColor);
    }

    void switchCalendarDayOfWeekWeekdaysColor(TextView textDayOfWeek) {
        Objects.requireNonNull(textDayOfWeek);

        int onColor = getOnWeekdaysColor();
        switchTextViewColorOnlyText(textDayOfWeek, onColor);
    }

    void switchCalendarDayOfWeekSaturdayColor(TextView textDayOfWeek) {
        Objects.requireNonNull(textDayOfWeek);

        int onColor = getOnSaturdayColor();
        switchTextViewColorOnlyText(textDayOfWeek, onColor);
    }

    void switchCalendarDayOfWeekSundayColor(TextView textDayOfWeek) {
        Objects.requireNonNull(textDayOfWeek);

        int onColor = getOnSundayColor();
        switchTextViewColorOnlyText(textDayOfWeek, onColor);
    }

    private int getCalendarSurfaceColor() {
        return getThemeColor().getSurfaceColor(getResources());
    }

    private int getOnWeekdaysColor() {
        return getThemeColor().getOnSurfaceColor(getResources());
    }

    private int getOnSaturdayColor() {
        return ResourcesCompat.getColor(getResources(), R.color.blue, null);
    }

    private int getOnSundayColor() {
        return ResourcesCompat.getColor(getResources(), R.color.red, null);
    }


}
