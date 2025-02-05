package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import java.util.Arrays


class ThemeColorPreference {

    companion object {
        @JvmField
        val PREFERENCES_KEY_COLOR: Preferences.Key<Int> = intPreferencesKey("theme_color")
    }

    private val themeColorNumber: Int

    constructor(themeColorNumber: Int) {
        val contains =
            Arrays.stream(ThemeColor.entries.toTypedArray())
                .anyMatch { x: ThemeColor -> x.toNumber() == themeColorNumber }
        require(contains)

        this.themeColorNumber = themeColorNumber
    }

    @JvmOverloads
    constructor(themeColor: ThemeColor = ThemeColor.entries[0]) {
        this.themeColorNumber = themeColor.toNumber()
    }

    fun setUpPreferences(mutablePreferences: MutablePreferences) {
        mutablePreferences[PREFERENCES_KEY_COLOR] = themeColorNumber
    }

    fun toThemeColor(): ThemeColor {
        return ThemeColor.of(themeColorNumber)
    }
}
