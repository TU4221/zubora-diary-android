package com.websarva.wings.android.zuboradiary.data.preferences

import com.websarva.wings.android.zuboradiary.data.model.ThemeColor


internal class ThemeColorPreference : UserPreference {

    companion object {
        val THEME_COLOR_DEFAULT_VALUE = ThemeColor.entries[0].toNumber()
    }

    val themeColorNumber: Int

    val themeColor: ThemeColor
        get() = ThemeColor.of(themeColorNumber)

    constructor(themeColorNumber: Int) {
        ThemeColor.of(themeColorNumber)

        this.themeColorNumber = themeColorNumber
    }

    constructor(themeColor: ThemeColor) {
        this.themeColorNumber = themeColor.toNumber()
    }

    constructor(): this(THEME_COLOR_DEFAULT_VALUE)
}
