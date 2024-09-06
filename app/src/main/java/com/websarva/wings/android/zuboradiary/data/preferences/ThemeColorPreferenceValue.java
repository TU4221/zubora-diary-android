package com.websarva.wings.android.zuboradiary.data.preferences;


import java.util.Arrays;

public class ThemeColorPreferenceValue {
    private final int themeColorNumber;

    public ThemeColorPreferenceValue(int themeColorNumber) {
        boolean contains =
                Arrays.stream(ThemeColor.values()).anyMatch(x -> x.getNumber() == themeColorNumber);

        if (!contains) {
            throw new IllegalArgumentException();
        }

        this.themeColorNumber = themeColorNumber;
    }

    public ThemeColorPreferenceValue(ThemeColor themeColor) {
        if (themeColor == null) {
            throw new NullPointerException();
        }

        this.themeColorNumber = themeColor.getNumber();
    }

    public int getThemeColorNumber() {
        return themeColorNumber;
    }

    public ThemeColor getThemeColor() {
        return ThemeColor.of(themeColorNumber);
    }
}
