package com.websarva.wings.android.zuboradiary.data.preferences

import com.websarva.wings.android.zuboradiary.data.model.ThemeColor


internal class ThemeColorPreference(
    val themeColorNumber: Int = ThemeColor.entries[0].toNumber()
) : UserPreference
