package com.websarva.wings.android.zuboradiary.data.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

class WeatherInfoAcquisitionPreference @JvmOverloads constructor(val isChecked: Boolean = false) {

    companion object {
        @JvmField
        val PREFERENCES_KEY_IS_CHECKED: Preferences.Key<Boolean> =
            booleanPreferencesKey("is_checked_weather_info_acquisition")
    }

    fun setUpPreferences(mutablePreferences: MutablePreferences) {
        mutablePreferences[PREFERENCES_KEY_IS_CHECKED] = isChecked
    }
}
