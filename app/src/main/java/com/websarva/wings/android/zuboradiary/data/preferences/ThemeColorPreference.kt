package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor


class ThemeColorPreference {

    // MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
    //      その為、@Suppress("RedundantSuppression")で警告回避。
    @Suppress("unused", "RedundantSuppression") // MEMO:デフォルトパラメータで使用する為、@Suppressで警告回避。
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
