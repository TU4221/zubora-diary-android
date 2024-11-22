package com.websarva.wings.android.zuboradiary.data.diary;

import android.content.Context;

import androidx.annotation.NonNull;

import com.websarva.wings.android.zuboradiary.R;

import java.util.Arrays;
import java.util.Objects;

public enum Condition {

    UNKNOWN(0, R.string.enum_condition_unknown),
    HAPPY(1, R.string.enum_condition_happy),
    GOOD(2, R.string.enum_condition_good),
    AVERAGE(3, R.string.enum_condition_average),
    POOR(4, R.string.enum_condition_poor),
    BAD(5, R.string.enum_condition_bad);

    private final int number;
    private final int stringResId;

    Condition(int number, int stringResId) {
        this.number = number;
        this.stringResId = stringResId;
    }

    @NonNull
    public static Condition of(int number) {
        return Arrays.stream(Condition.values()).filter(x -> x.toNumber() == number).findFirst().get();
    }

    @NonNull
    public static Condition of(Context context, String strCondition) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(strCondition);

        return Arrays.stream(Condition.values())
                .filter(x -> x.toString(context).equals(strCondition)).findFirst().get();
    }

    @NonNull
    public String toString(Context context) {
        Objects.requireNonNull(context);

        String string = context.getString(stringResId);
        return Objects.requireNonNull(string);
    }

    public int toNumber() {
        return number;
    }
}
