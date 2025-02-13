package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey


class ThemeColorPreference {

    companion object {
        private val THEME_COLOR_DEFAULT_VALUE = ThemeColor.entries[0]
    }

    private val themeColorPreferenceKey = intPreferencesKey("theme_color")

    private val themeColorNumber: Int

    val themeColor: ThemeColor
        get() = ThemeColor.of(themeColorNumber)

    constructor(preferences: Preferences) {
        this.themeColorNumber =
            preferences[themeColorPreferenceKey] ?: THEME_COLOR_DEFAULT_VALUE.toNumber()
    }

    @JvmOverloads
    constructor(themeColor: ThemeColor = THEME_COLOR_DEFAULT_VALUE) {
        this.themeColorNumber = themeColor.toNumber()
    }

    fun setUpPreferences(mutablePreferences: MutablePreferences) {
        mutablePreferences[themeColorPreferenceKey] = themeColorNumber
    }
}
