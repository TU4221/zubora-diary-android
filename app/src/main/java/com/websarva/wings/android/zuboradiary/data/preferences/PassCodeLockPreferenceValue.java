package com.websarva.wings.android.zuboradiary.data.preferences;

import android.net.ipsec.ike.IkeSaProposal;

import androidx.annotation.Nullable;

public class PassCodeLockPreferenceValue {

    private final boolean isChecked;
    private final int code;

    public PassCodeLockPreferenceValue(Boolean isChecked,@Nullable Integer code) {
        if (isChecked == null) {
            throw new NullPointerException();
        }
        if (isChecked && code == null) {
            throw new NullPointerException();
        }
        if (isChecked && code < 0 || code > 9999) {
            throw new IllegalArgumentException();
        }

        this.isChecked = isChecked;
        if (isChecked) {
            this.code = code;
        } else {
            this.code = -1;
        }
    }

    public boolean getIsChecked() {
        return isChecked;
    }

    public int getCode() {
        return code;
    }
}
