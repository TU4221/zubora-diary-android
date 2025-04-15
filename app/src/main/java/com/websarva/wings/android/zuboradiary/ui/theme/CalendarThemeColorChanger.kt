package com.websarva.wings.android.zuboradiary.ui.theme

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor

internal class CalendarThemeColorChanger(context: Context, themeColor: ThemeColor) :
    ThemeColorChanger(context, themeColor) {
    fun applyCalendarTodayColor(textDay: TextView, viewDot: View) {
        val color = themeColor.getSecondaryContainerColor(resources)
        val onColor = themeColor.getOnSecondaryContainerColor(resources)
        applyCalendarDayColor(color, onColor, textDay, viewDot)
    }

    fun applyCalendarSelectedDayColor(textDay: TextView, viewDot: View) {
        val color = themeColor.getSecondaryColor(resources)
        val onColor = themeColor.getOnSecondaryColor(resources)
        applyCalendarDayColor(color, onColor, textDay, viewDot)
    }

    fun applyCalendarWeekdaysColor(textDay: TextView, viewDot: View) {
        val color = calendarSurfaceColor
        val onColor = onWeekdaysColor
        applyCalendarDayColor(color, onColor, textDay, viewDot)
    }

    fun applyCalendarSaturdayColor(textDay: TextView, viewDot: View) {
        val color = calendarSurfaceColor
        val onColor = onSaturdayColor
        applyCalendarDayColor(color, onColor, textDay, viewDot)
    }

    fun applyCalendarSundayColor(textDay: TextView, viewDot: View) {
        val color = calendarSurfaceColor
        val onColor = onSundayColor
        applyCalendarDayColor(color, onColor, textDay, viewDot)
    }

    private fun applyCalendarDayColor(color: Int, onColor: Int, textDay: TextView, viewDot: View) {
        val drawable =
            checkNotNull(
                ResourcesCompat.getDrawable(resources, R.drawable.bg_calendar_day, null)
            )

        applyDrawableColor(drawable, color)
        textDay.background = drawable

        applyTextViewColorOnlyText(textDay, onColor)
        applyViewColor(viewDot, onColor)
    }

    fun applyCalendarDayOfWeekWeekdaysColor(textDayOfWeek: TextView) {
        val onColor = onWeekdaysColor
        applyTextViewColorOnlyText(textDayOfWeek, onColor)
    }

    fun applyCalendarDayOfWeekSaturdayColor(textDayOfWeek: TextView) {
        val onColor = onSaturdayColor
        applyTextViewColorOnlyText(textDayOfWeek, onColor)
    }

    fun applyCalendarDayOfWeekSundayColor(textDayOfWeek: TextView) {
        val onColor = onSundayColor
        applyTextViewColorOnlyText(textDayOfWeek, onColor)
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
