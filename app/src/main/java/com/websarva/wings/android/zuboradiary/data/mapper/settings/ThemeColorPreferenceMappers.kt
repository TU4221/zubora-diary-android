package com.websarva.wings.android.zuboradiary.data.mapper.settings

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColorPreference
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.domain.model.settings.ThemeColorSetting

internal fun ThemeColorPreference.toDomainModel(): ThemeColorSetting {
    val themeColor = ThemeColor.of(themeColorNumber)
    return ThemeColorSetting(themeColor)
}

internal fun ThemeColorSetting.toDataModel(): ThemeColorPreference {
    return ThemeColorPreference(themeColor.number)
}
