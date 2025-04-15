package com.websarva.wings.android.zuboradiary.ui.theme

import android.content.Context
import android.widget.TextView
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor

internal class SettingsThemeColorChanger(context: Context, themeColor: ThemeColor)
    : ThemeColorChanger(context, themeColor) {

    fun applySettingItemSectionColor(textViewList: List<TextView>) {
        val color = themeColor.getSecondaryContainerColor(resources)
        val onColor = themeColor.getOnSecondaryContainerColor(resources)
        applyTextViewsColor(textViewList, color, onColor)
    }

    fun applySettingItemIconColor(textViewList: List<TextView>) {
        val color = themeColor.getOnSurfaceVariantColor(resources)
        applyTextViewsColorOnlyIcon(textViewList, color)
    }
}
