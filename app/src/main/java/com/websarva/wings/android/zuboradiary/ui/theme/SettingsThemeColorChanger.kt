package com.websarva.wings.android.zuboradiary.ui.theme

import android.widget.TextView
import com.websarva.wings.android.zuboradiary.ui.utils.asOnSecondaryContainerColor
import com.websarva.wings.android.zuboradiary.ui.utils.asOnSurfaceVariantColor
import com.websarva.wings.android.zuboradiary.ui.utils.asSecondaryContainerColor
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi

internal class SettingsThemeColorChanger : ThemeColorChanger() {

    fun applySettingItemSectionColor(textViewList: List<TextView>, themeColor: ThemeColorUi) {
        require(textViewList.isNotEmpty())
        val resources = textViewList.first().requireResources()

        val color = themeColor.asSecondaryContainerColor(resources)
        val onColor = themeColor.asOnSecondaryContainerColor(resources)
        applyTextViewsColor(textViewList, color, onColor)
    }

    fun applySettingItemIconColor(textViewList: List<TextView>, themeColor: ThemeColorUi) {
        require(textViewList.isNotEmpty())
        val resources = textViewList.first().requireResources()

        val color = themeColor.asOnSurfaceVariantColor(resources)
        applyTextViewsColorOnlyIcon(textViewList, color)
    }
}
