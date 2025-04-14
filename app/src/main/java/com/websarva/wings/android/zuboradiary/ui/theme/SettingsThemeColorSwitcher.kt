package com.websarva.wings.android.zuboradiary.ui.theme

import android.content.Context
import android.widget.TextView
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor

internal class SettingsThemeColorSwitcher(context: Context, themeColor: ThemeColor)
    : ThemeColorSwitcher(context, themeColor) {

    fun switchSettingItemSectionColor(textViewList: List<TextView>) {
        val color = themeColor.getSecondaryContainerColor(resources)
        val onColor = themeColor.getOnSecondaryContainerColor(resources)
        switchTextViewsColor(textViewList, color, onColor)
    }

    fun switchSettingItemIconColor(textViewList: List<TextView>) {
        val color = themeColor.getOnSurfaceVariantColor(resources)
        switchTextViewsColorOnlyIcon(textViewList, color)
    }
}
