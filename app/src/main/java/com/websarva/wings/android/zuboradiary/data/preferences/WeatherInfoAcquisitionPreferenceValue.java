package com.websarva.wings.android.zuboradiary.data.preferences;

public class WeatherInfoAcquisitionPreferenceValue {

    private final boolean isChecked;

    public WeatherInfoAcquisitionPreferenceValue(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public boolean getIsChecked() {
        return isChecked;
    }
}
