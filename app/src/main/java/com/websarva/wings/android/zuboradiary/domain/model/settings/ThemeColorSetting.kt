package com.websarva.wings.android.zuboradiary.domain.model.settings

import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor

internal data class ThemeColorSetting(
    val themeColor: ThemeColor = ThemeColor.entries[0]
) : UserSetting
