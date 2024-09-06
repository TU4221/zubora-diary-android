package com.websarva.wings.android.zuboradiary.data.preferences;

import androidx.annotation.Nullable;

public class GettingWeatherInformationPreferenceValue {
    private final boolean isChecked;

    public GettingWeatherInformationPreferenceValue(Boolean isChecked) {
        if (isChecked == null) {
            throw new NullPointerException();
        }

        this.isChecked = isChecked;
    }

    public boolean getIsChecked() {
        return isChecked;
    }
}
