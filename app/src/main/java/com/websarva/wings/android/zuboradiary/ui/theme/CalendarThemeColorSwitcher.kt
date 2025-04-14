package com.websarva.wings.android.zuboradiary.ui.theme

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor

internal class CalendarThemeColorSwitcher(context: Context, themeColor: ThemeColor) :
    ThemeColorSwitcher(context, themeColor) {
    fun switchCalendarTodayColor(textDay: TextView, viewDot: View) {
        val color = themeColor.getSecondaryContainerColor(resources)
        val onColor = themeColor.getOnSecondaryContainerColor(resources)
        switchCalendarDayColor(color, onColor, textDay, viewDot)
    }

    fun switchCalendarSelectedDayColor(textDay: TextView, viewDot: View) {
        val color = themeColor.getSecondaryColor(resources)
        val onColor = themeColor.getOnSecondaryColor(resources)
        switchCalendarDayColor(color, onColor, textDay, viewDot)
    }

    fun switchCalendarWeekdaysColor(textDay: TextView, viewDot: View) {
        val color = calendarSurfaceColor
        val onColor = onWeekdaysColor
        switchCalendarDayColor(color, onColor, textDay, viewDot)
    }

    fun switchCalendarSaturdayColor(textDay: TextView, viewDot: View) {
        val color = calendarSurfaceColor
        val onColor = onSaturdayColor
        switchCalendarDayColor(color, onColor, textDay, viewDot)
    }

    fun switchCalendarSundayColor(textDay: TextView, viewDot: View) {
        val color = calendarSurfaceColor
        val onColor = onSundayColor
        switchCalendarDayColor(color, onColor, textDay, viewDot)
    }

    private fun switchCalendarDayColor(color: Int, onColor: Int, textDay: TextView, viewDot: View) {
        val drawable =
            checkNotNull(
                ResourcesCompat.getDrawable(resources, R.drawable.bg_calendar_day, null)
            )

        switchDrawableColor(drawable, color)
        textDay.background = drawable

        switchTextViewColorOnlyText(textDay, onColor)
        switchViewColor(viewDot, onColor)
    }

    fun switchCalendarDayOfWeekWeekdaysColor(textDayOfWeek: TextView) {
        val onColor = onWeekdaysColor
        switchTextViewColorOnlyText(textDayOfWeek, onColor)
    }

    fun switchCalendarDayOfWeekSaturdayColor(textDayOfWeek: TextView) {
        val onColor = onSaturdayColor
        switchTextViewColorOnlyText(textDayOfWeek, onColor)
    }

    fun switchCalendarDayOfWeekSundayColor(textDayOfWeek: TextView) {
        val onColor = onSundayColor
        switchTextViewColorOnlyText(textDayOfWeek, onColor)
    }

    private val calendarSurfaceColor
        get() = themeColor.getSurfaceColor(resources)

    private val onWeekdaysColor
        get() = themeColor.getOnSurfaceColor(resources)

    private val onSaturdayColor
        get() = ResourcesCompat.getColor(resources, R.color.blue, null)

    private val onSundayColor
        get() = ResourcesCompat.getColor(resources, R.color.red, null)
}
