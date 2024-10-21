package com.websarva.wings.android.zuboradiary.data.preferences;

public class WeatherInfoAcquisitionPreferenceValue {
    private final boolean isChecked;

    public WeatherInfoAcquisitionPreferenceValue(Boolean isChecked) {
        if (isChecked == null) {
            throw new NullPointerException();
        }

        this.isChecked = isChecked;
    }

    public boolean getIsChecked() {
        return isChecked;
    }
}
