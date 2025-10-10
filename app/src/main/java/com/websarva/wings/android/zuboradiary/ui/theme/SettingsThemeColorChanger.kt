package com.websarva.wings.android.zuboradiary.ui.theme

import android.widget.TextView
import com.websarva.wings.android.zuboradiary.ui.utils.asOnSecondaryContainerColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.asOnSurfaceVariantColorInt
import com.websarva.wings.android.zuboradiary.ui.utils.asSecondaryContainerColorInt
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi

internal class SettingsThemeColorChanger : ThemeColorChanger() {

    fun applySettingItemSectionColor(textViewList: List<TextView>, themeColor: ThemeColorUi) {
        require(textViewList.isNotEmpty())
        val resources = textViewList.first().requireResources()

        val color = themeColor.asSecondaryContainerColorInt(resources)
        val onColor = themeColor.asOnSecondaryContainerColorInt(resources)
        applyTextViewsColor(textViewList, color, onColor)
    }

    fun applySettingItemIconColor(textViewList: List<TextView>, themeColor: ThemeColorUi) {
        require(textViewList.isNotEmpty())
        val resources = textViewList.first().requireResources()

        val color = themeColor.asOnSurfaceVariantColorInt(resources)
        applyTextViewsColorOnlyIcon(textViewList, color)
    }
}
