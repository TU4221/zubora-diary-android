package com.websarva.wings.android.zuboradiary.ui.theme

import android.widget.TextView
import com.websarva.wings.android.zuboradiary.ui.model.ThemeColorUi

internal class SettingsThemeColorChanger : ThemeColorChanger() {

    fun applySettingItemSectionColor(textViewList: List<TextView>, themeColor: ThemeColorUi) {
        require(textViewList.isNotEmpty())
        val resources = textViewList.first().requireResources()

        val color = themeColor.getSecondaryContainerColor(resources)
        val onColor = themeColor.getOnSecondaryContainerColor(resources)
        applyTextViewsColor(textViewList, color, onColor)
    }

    fun applySettingItemIconColor(textViewList: List<TextView>, themeColor: ThemeColorUi) {
        require(textViewList.isNotEmpty())
        val resources = textViewList.first().requireResources()

        val color = themeColor.getOnSurfaceVariantColor(resources)
        applyTextViewsColorOnlyIcon(textViewList, color)
    }
}
