package com.websarva.wings.android.zuboradiary.data.preferences;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;

import java.util.Objects;

public class WeatherInfoAcquisitionPreference {

    static final Preferences.Key<Boolean> PREFERENCES_KEY_IS_CHECKED =
            PreferencesKeys.booleanKey("is_checked_weather_info_acquisition");
    private final boolean isChecked;

    public WeatherInfoAcquisitionPreference(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public WeatherInfoAcquisitionPreference() {
        this(false);
    }

    void setUpPreferences(MutablePreferences mutablePreferences) {
        Objects.requireNonNull(mutablePreferences);

        mutablePreferences.set(PREFERENCES_KEY_IS_CHECKED, isChecked);
    }

    public boolean getIsChecked() {
        return isChecked;
    }
}
