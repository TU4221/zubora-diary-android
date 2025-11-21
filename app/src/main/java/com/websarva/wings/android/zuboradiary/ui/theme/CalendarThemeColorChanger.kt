package com.websarva.wings.android.zuboradiary.ui.theme

import android.content.res.Resources
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.utils.asOnSecondaryColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.asOnSecondaryContainerColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.asOnSurfaceColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.asSecondaryColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.asSecondaryContainerColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.asSurfaceColorInt
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi

/**
 * カレンダーのViewに特化したテーマカラーを動的に変更するためのヘルパークラス。
 *
 * このクラスは、[ThemeColorChanger]を継承し、カレンダーの日付セルや曜日の凡例などの配色を適用する責務を持つ。
 */
internal class CalendarThemeColorChanger : ThemeColorChanger() {

    // region Day Of Week
    /**
     * カレンダーの平日の曜日セルの色を適用する。
     * @param textDayOfWeek 曜日を表示するTextView。
     * @param themeColor 適用するテーマカラー。
     */
    fun applyCalendarDayOfWeekWeekdaysColor(textDayOfWeek: TextView, themeColor: ThemeColorUi) {
        val resources = textDayOfWeek.resources

        val onColor = getOnWeekdaysColor(themeColor, resources)
        applyTextViewColorOnlyText(textDayOfWeek, onColor)
    }

    /**
     * カレンダーの土曜の曜日セルの色を適用する。
     * @param textDayOfWeek 曜日を表示するTextView。
     */
    fun applyCalendarDayOfWeekSaturdayColor(textDayOfWeek: TextView) {
        val resources = textDayOfWeek.resources

        val onColor = getOnSaturdayColor(resources)
        applyTextViewColorOnlyText(textDayOfWeek, onColor)
    }

    /**
     * カレンダーの日曜の曜日セルの色を適用する。
     * @param textDayOfWeek 曜日を表示するTextView。
     */
    fun applyCalendarDayOfWeekSundayColor(textDayOfWeek: TextView) {
        val resources = textDayOfWeek.resources

        val onColor = getOnSundayColor(resources)
        applyTextViewColorOnlyText(textDayOfWeek, onColor)
    }
    // endregion

    // region Day
    /**
     * カレンダーの「今日」の日付セルの色を適用する。
     * @param textDay 日付を表示するTextView。
     * @param viewDot 日記の有無を示すドットView。
     * @param themeColor 適用するテーマカラー。
     */
    fun applyCalendarTodayColor(textDay: TextView, viewDot: View, themeColor: ThemeColorUi) {
        val resources = textDay.resources

        val color = themeColor.asSecondaryContainerColorInt(resources)
        val onColor = themeColor.asOnSecondaryContainerColorInt(resources)
        applyCalendarDayColor(color, onColor, textDay, viewDot)
    }

    /**
     * カレンダーで選択された日付セルの色を適用する。
     * @param textDay 日付を表示するTextView。
     * @param viewDot 日記の有無を示すドットView。
     * @param themeColor 適用するテーマカラー。
     */
    fun applyCalendarSelectedDayColor(textDay: TextView, viewDot: View, themeColor: ThemeColorUi) {
        val resources = textDay.resources

        val color = themeColor.asSecondaryColorInt(resources)
        val onColor = themeColor.asOnSecondaryColorInt(resources)
        applyCalendarDayColor(color, onColor, textDay, viewDot)
    }

    /**
     * カレンダーの平日日付セルの色を適用する。
     * @param textDay 日付を表示するTextView。
     * @param viewDot 日記の有無を示すドットView。
     * @param themeColor 適用するテーマカラー。
     */
    fun applyCalendarWeekdaysColor(textDay: TextView, viewDot: View, themeColor: ThemeColorUi) {
        val resources = textDay.resources

        val color = getCalendarSurfaceColor(themeColor, resources)
        val onColor = getOnWeekdaysColor(themeColor, resources)
        applyCalendarDayColor(color, onColor, textDay, viewDot)
    }

    /**
     * カレンダーの土曜日付セルの色を適用する。
     * @param textDay 日付を表示するTextView。
     * @param viewDot 日記の有無を示すドットView。
     * @param themeColor 適用するテーマカラー。
     */
    fun applyCalendarSaturdayColor(textDay: TextView, viewDot: View, themeColor: ThemeColorUi) {
        val resources = textDay.resources

        val color = getCalendarSurfaceColor(themeColor, resources)
        val onColor = getOnSaturdayColor(resources)
        applyCalendarDayColor(color, onColor, textDay, viewDot)
    }

    /**
     * カレンダーの日曜日付セルの色を適用する。
     * @param textDay 日付を表示するTextView。
     * @param viewDot 日記の有無を示すドットView。
     * @param themeColor 適用するテーマカラー。
     */
    fun applyCalendarSundayColor(textDay: TextView, viewDot: View, themeColor: ThemeColorUi) {
        val resources = textDay.resources

        val color = getCalendarSurfaceColor(themeColor, resources)
        val onColor = getOnSundayColor(resources)
        applyCalendarDayColor(color, onColor, textDay, viewDot)
    }

    /**
     * カレンダーの日付セル（背景、日付テキスト、ドット）に指定された色を適用する共通ヘルパーメソッド。
     * @param color 背景色。
     * @param onColor テキストとドットの色。
     * @param textDay 日付を表示するTextView。
     * @param viewDot 日記の有無を示すドットView。
     */
    private fun applyCalendarDayColor(color: Int, onColor: Int, textDay: TextView, viewDot: View) {
        val resources = textDay.resources

        val drawable =
            checkNotNull(
                ResourcesCompat.getDrawable(resources, R.drawable.bg_calendar_day, null)
            )

        applyDrawableColor(drawable, color)
        (textDay.parent as? View)?.background = drawable

        applyTextViewColorOnlyText(textDay, onColor)
        applyViewColor(viewDot, onColor)
    }
    // endregion

    // region Color
    /**
     * 現在のテーマに応じたカレンダーの背景色を取得する。
     * @param themeColor 現在のテーマカラー。
     * @param resources リソース。
     * @return サーフェス色のInt値。
     */
    private fun getCalendarSurfaceColor(themeColor: ThemeColorUi, resources: Resources): Int {
        return themeColor.asSurfaceColorInt(resources)
    }

    /**
     * 現在のテーマに応じた平日のテキスト色を取得する。
     * @param themeColor 現在のテーマカラー。
     * @param resources リソース。
     * @return テキスト色のInt値。
     */
    private fun getOnWeekdaysColor(themeColor: ThemeColorUi, resources: Resources): Int {
        return themeColor.asOnSurfaceColorInt(resources)
    }

    /**
     * 土曜日用のテキスト色（青）を取得する。
     * @param resources リソース。
     * @return テキスト色のInt値。
     */
    private fun getOnSaturdayColor(resources: Resources): Int {
        return ResourcesCompat.getColor(resources, R.color.blue, null)
    }

    /**
     * 日曜日用のテキスト色（赤）を取得する。
     * @param resources リソース。
     * @return テキスト色のInt値。
     */
    private fun getOnSundayColor(resources: Resources): Int {
        return ResourcesCompat.getColor(resources, R.color.red, null)
    }
    // endregion
}
