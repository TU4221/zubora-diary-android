package com.websarva.wings.android.zuboradiary.data.preferences;


import java.util.Arrays;
import java.util.Objects;

public class ThemeColorPreferenceValue {

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

    public int getThemeColorNumber() {
        return themeColorNumber;
    }

    public ThemeColor getThemeColor() {
        return ThemeColor.of(themeColorNumber);
    }
}
