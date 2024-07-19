package com.websarva.wings.android.zuboradiary.data.diary;

import android.content.Context;

import com.websarva.wings.android.zuboradiary.R;

public enum Conditions {
    UNKNOWN(0, R.string.enum_conditions_unknown),
    HAPPY(1, R.string.enum_conditions_happy),
    GOOD(2, R.string.enum_conditions_good),
    AVERAGE(3, R.string.enum_conditions_average),
    POOR(4, R.string.enum_conditions_poor),
    BAD(5, R.string.enum_conditions_bad);
    private int conditionNumber;
    private int conditionNameResId;

    private Conditions(int conditionNumber, int weatherNameResId) {
        this.conditionNumber = conditionNumber;
        this.conditionNameResId = weatherNameResId;
    }

    public String toString(Context context) {
        return context.getString(this.conditionNameResId);
    }

    public int toConditionNumber() {
        return this.conditionNumber;
    }
}
