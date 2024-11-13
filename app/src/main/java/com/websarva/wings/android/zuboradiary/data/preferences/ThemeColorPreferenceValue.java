package com.websarva.wings.android.zuboradiary.data.preferences;


import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;

import java.util.Arrays;
import java.util.Objects;

public class ThemeColorPreferenceValue {

    static final Preferences.Key<Integer> PREFERENCES_KEY_COLOR =
                                                        PreferencesKeys.intKey("theme_color");
    private final int themeColorNumber;

    public ThemeColorPreferenceValue(int themeColorNumber) {
        boolean contains =
                Arrays.stream(ThemeColor.values()).anyMatch(x -> x.toNumber() == themeColorNumber);
        if (!contains) throw new IllegalArgumentException();

        this.themeColorNumber = themeColorNumber;
    }

    public ThemeColorPreferenceValue(ThemeColor themeColor) {
        Objects.requireNonNull(themeColor);

        this.themeColorNumber = themeColor.toNumber();
    }

    public ThemeColorPreferenceValue() {
        this(ThemeColor.values()[0]);
    }

    void setUpPreferences(MutablePreferences mutablePreferences) {
        Objects.requireNonNull(mutablePreferences);

        mutablePreferences.set(PREFERENCES_KEY_COLOR, themeColorNumber);
    }

    public int getThemeColorNumber() {
        return themeColorNumber;
    }

    public ThemeColor getThemeColor() {
        return ThemeColor.of(themeColorNumber);
    }
}
