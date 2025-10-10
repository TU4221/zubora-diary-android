package com.websarva.wings.android.zuboradiary.ui.theme

import android.content.res.Resources
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.utils.asOnSecondaryColor
import com.websarva.wings.android.zuboradiary.ui.utils.asOnSecondaryContainerColor
import com.websarva.wings.android.zuboradiary.ui.utils.asOnSurfaceColor
import com.websarva.wings.android.zuboradiary.ui.utils.asSecondaryColor
import com.websarva.wings.android.zuboradiary.ui.utils.asSecondaryContainerColor
import com.websarva.wings.android.zuboradiary.ui.utils.asSurfaceColor
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi

internal class CalendarThemeColorChanger : ThemeColorChanger() {

    fun applyCalendarTodayColor(textDay: TextView, viewDot: View, themeColor: ThemeColorUi) {
        val resources = textDay.requireResources()

        val color = themeColor.asSecondaryContainerColor(resources)
        val onColor = themeColor.asOnSecondaryContainerColor(resources)
        applyCalendarDayColor(color, onColor, textDay, viewDot)
    }

    fun applyCalendarSelectedDayColor(textDay: TextView, viewDot: View, themeColor: ThemeColorUi) {
        val resources = textDay.requireResources()

        val color = themeColor.asSecondaryColor(resources)
        val onColor = themeColor.asOnSecondaryColor(resources)
        applyCalendarDayColor(color, onColor, textDay, viewDot)
    }

    fun applyCalendarWeekdaysColor(textDay: TextView, viewDot: View, themeColor: ThemeColorUi) {
        val resources = textDay.requireResources()

        val color = getCalendarSurfaceColor(themeColor, resources)
        val onColor = getOnWeekdaysColor(themeColor, resources)
        applyCalendarDayColor(color, onColor, textDay, viewDot)
    }

    fun applyCalendarSaturdayColor(textDay: TextView, viewDot: View, themeColor: ThemeColorUi) {
        val resources = textDay.requireResources()

        val color = getCalendarSurfaceColor(themeColor, resources)
        val onColor = getOnSaturdayColor(resources)
        applyCalendarDayColor(color, onColor, textDay, viewDot)
    }

    fun applyCalendarSundayColor(textDay: TextView, viewDot: View, themeColor: ThemeColorUi) {
        val resources = textDay.requireResources()

        val color = getCalendarSurfaceColor(themeColor, resources)
        val onColor = getOnSundayColor(resources)
        applyCalendarDayColor(color, onColor, textDay, viewDot)
    }

    private fun applyCalendarDayColor(color: Int, onColor: Int, textDay: TextView, viewDot: View) {
        val resources = textDay.requireResources()

        val drawable =
            checkNotNull(
                ResourcesCompat.getDrawable(resources, R.drawable.bg_calendar_day, null)
            )

        applyDrawableColor(drawable, color)
        textDay.background = drawable

        applyTextViewColorOnlyText(textDay, onColor)
        applyViewColor(viewDot, onColor)
    }

    fun applyCalendarDayOfWeekWeekdaysColor(textDayOfWeek: TextView, themeColor: ThemeColorUi) {
        val resources = textDayOfWeek.requireResources()

        val onColor = getOnWeekdaysColor(themeColor, resources)
        applyTextViewColorOnlyText(textDayOfWeek, onColor)
    }

    fun applyCalendarDayOfWeekSaturdayColor(textDayOfWeek: TextView) {
        val resources = textDayOfWeek.requireResources()

        val onColor = getOnSaturdayColor(resources)
        applyTextViewColorOnlyText(textDayOfWeek, onColor)
    }

    fun applyCalendarDayOfWeekSundayColor(textDayOfWeek: TextView) {
        val resources = textDayOfWeek.requireResources()

        val onColor = getOnSundayColor(resources)
        applyTextViewColorOnlyText(textDayOfWeek, onColor)
    }

    private fun getCalendarSurfaceColor(themeColor: ThemeColorUi, resources: Resources): Int {
        return themeColor.asSurfaceColor(resources)
    }

    private fun getOnWeekdaysColor(themeColor: ThemeColorUi, resources: Resources): Int {
        return themeColor.asOnSurfaceColor(resources)
    }

    private fun getOnSaturdayColor(resources: Resources): Int {
        return ResourcesCompat.getColor(resources, R.color.blue, null)
    }

    private fun getOnSundayColor(resources: Resources): Int {
        return ResourcesCompat.getColor(resources, R.color.red, null)
    }
}
