package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

class WeatherInfoAcquisitionPreference {

    // MEMO:@Suppress("unused")が不要と警告が発生したので削除したが、"unused"警告が再発する。
    //      その為、@Suppress("RedundantSuppression")で警告回避。
    @Suppress("unused", "RedundantSuppression") // MEMO:デフォルトパラメータで使用する為、@Suppressで警告回避。
    companion object {
        private const val IS_CHECKED_DEFAULT_VALUE = false
    }

    private val isCheckedPreferenceKey =
        booleanPreferencesKey("is_checked_weather_info_acquisition")

    val isChecked: Boolean

    constructor(preferences: Preferences) {
        this.isChecked = preferences[isCheckedPreferenceKey] ?: IS_CHECKED_DEFAULT_VALUE
    }

    @JvmOverloads
    constructor(isChecked: Boolean = IS_CHECKED_DEFAULT_VALUE) {
        this.isChecked = isChecked
    }

    fun setUpPreferences(mutablePreferences: MutablePreferences) {
        mutablePreferences[isCheckedPreferenceKey] = isChecked
    }
}
