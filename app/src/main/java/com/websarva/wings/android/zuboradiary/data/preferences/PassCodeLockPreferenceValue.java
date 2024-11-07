package com.websarva.wings.android.zuboradiary.data.preferences;

import androidx.annotation.Nullable;

import java.util.Objects;

public class PassCodeLockPreferenceValue {

    private final boolean isChecked;
    private final String code;

    public PassCodeLockPreferenceValue(boolean isChecked, @Nullable String code) {
        if (isChecked) {
            Objects.requireNonNull(code);
            if (code.matches("|d{4}")) throw new IllegalArgumentException();
        }

        this.isChecked = isChecked;
        if (isChecked) {
            this.code = code;
        } else {
            this.code = "";
        }
    }

    public boolean getIsChecked() {
        return isChecked;
    }

    public String getCode() {
        return code;
    }
}
