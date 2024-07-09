package com.websarva.wings.android.zuboradiary.data.settings;

import com.websarva.wings.android.zuboradiary.R;

// CAUTION:要素の追加、順序変更を行った時はThemeColorPickerDialogFragment、string.xmlを修正すること。
public enum ThemeColors {
    WHITE(0, R.string.dialog_fragment_number_picker_theme_color_white),
    BLACK(1, R.string.dialog_fragment_number_picker_theme_color_black),
    RED(2, R.string.dialog_fragment_number_picker_theme_color_red),
    BLUE(3, R.string.dialog_fragment_number_picker_theme_color_blue),
    GREEN(4, R.string.dialog_fragment_number_picker_theme_color_green),
    YELLOW(5, R.string.dialog_fragment_number_picker_theme_color_yellow);

    private int themeColorNumber;
    private int themeColorNameResId;

    private ThemeColors(int themeColorNumber, int themeColorNameResId) {
        this.themeColorNumber = themeColorNumber;
        this.themeColorNameResId = themeColorNameResId;
    }

    public ThemeColors getThemeColor(int themeColorNumber) {
        for (ThemeColors themeColor: ThemeColors.values()) {
            if (themeColor.getThemeColorNumber() == themeColorNumber) {
                return themeColor;
            }
        }
        return null;
    }

    public int getThemeColorNumber() {
        return this.themeColorNumber;
    }

    public int getThemeColorNameResId() {
        return this.themeColorNameResId;
    }
}
