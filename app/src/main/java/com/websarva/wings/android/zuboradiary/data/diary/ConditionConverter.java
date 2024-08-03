package com.websarva.wings.android.zuboradiary.data.diary;

import android.content.Context;

import androidx.annotation.Nullable;

public class ConditionConverter {
    public Conditions toCondition(@Nullable Integer conditionCode) {
        if (conditionCode == null) {
            return Conditions.UNKNOWN;
        }
        for (Conditions condition: Conditions.values()) {
            if (conditionCode == condition.toConditionNumber()) {
                return condition;
            }
        }
        return Conditions.UNKNOWN;
    }
    public Conditions toCondition(Context context, String strCondition) {
        for (Conditions condition: Conditions.values()) {
            if (strCondition.equals(condition.toString(context))) {
                return condition;
            }
        }
        return Conditions.UNKNOWN;
    }
}
