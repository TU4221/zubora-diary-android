package com.websarva.wings.android.zuboradiary.ui.theme

import android.widget.TextView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor

internal class SettingsThemeColorChanger : ThemeColorChanger() {

    fun applySettingItemSectionColor(textViewList: List<TextView>, themeColor: ThemeColor) {
        require(textViewList.isNotEmpty())
        val resources = textViewList.first().requireResources()

        val color = themeColor.getSecondaryContainerColor(resources)
        val onColor = themeColor.getOnSecondaryContainerColor(resources)
        applyTextViewsColor(textViewList, color, onColor)
    }

    fun applySettingItemIconColor(textViewList: List<TextView>, themeColor: ThemeColor) {
        require(textViewList.isNotEmpty())
        val resources = textViewList.first().requireResources()

        val color = themeColor.getOnSurfaceVariantColor(resources)
        applyTextViewsColorOnlyIcon(textViewList, color)
    }
}
