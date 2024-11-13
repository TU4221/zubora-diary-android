package com.websarva.wings.android.zuboradiary.data.preferences;

import androidx.annotation.Nullable;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;

import java.util.Objects;

public class PassCodeLockPreferenceValue {

    static final Preferences.Key<Boolean> PREFERENCES_KEY_IS_CHECKED =
            PreferencesKeys.booleanKey("is_checked_passcode_lock");
    static final Preferences.Key<String> PREFERENCES_KEY_PASSCODE = PreferencesKeys.stringKey("passcode");
    private final boolean isChecked;
    private final String passCode;

    public PassCodeLockPreferenceValue(boolean isChecked, @Nullable String passCode) {
        if (isChecked) {
            Objects.requireNonNull(passCode);
            if (passCode.matches("|d{4}")) throw new IllegalArgumentException();
        }

        this.isChecked = isChecked;
        if (isChecked) {
            this.passCode = passCode;
        } else {
            this.passCode = "";
        }
    }

    public PassCodeLockPreferenceValue() {
        this(false, "");
    }

    void setUpPreferences(MutablePreferences mutablePreferences) {
        Objects.requireNonNull(mutablePreferences);

        mutablePreferences.set(PREFERENCES_KEY_IS_CHECKED, isChecked);
        mutablePreferences.set(PREFERENCES_KEY_PASSCODE, passCode);
    }

    public boolean getIsChecked() {
        return isChecked;
    }

    public String getPassCode() {
        return passCode;
    }
}
