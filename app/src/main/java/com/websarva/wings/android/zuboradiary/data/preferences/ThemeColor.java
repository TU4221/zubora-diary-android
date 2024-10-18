package com.websarva.wings.android.zuboradiary.data.preferences;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.websarva.wings.android.zuboradiary.R;

import java.util.Arrays;

// CAUTION:要素の追加、順序変更を行った時はThemeColorPickerDialogFragment、string.xmlを修正すること。
public enum ThemeColor {
    WHITE(0, R.string.dialog_fragment_number_picker_theme_color_white),
    BLACK(1, R.string.dialog_fragment_number_picker_theme_color_black),
    RED(2, R.string.dialog_fragment_number_picker_theme_color_red),
    BLUE(3, R.string.dialog_fragment_number_picker_theme_color_blue),
    GREEN(4, R.string.dialog_fragment_number_picker_theme_color_green),
    YELLOW(5, R.string.dialog_fragment_number_picker_theme_color_yellow);

    private final int number;
    private final int stringResId;

    ThemeColor(int number, int stringResId) {
        this.number = number;
        this.stringResId = stringResId;
    }

    @NonNull
    public static ThemeColor of(int number) {
        return Arrays.stream(ThemeColor.values())
                .filter(x -> x.getNumber() == number)
                .findFirst().get();
    }

    public int getNumber() {
        return this.number;
    }

    public String toSting(Context context) {
        if (context == null) {
            throw new NullPointerException();
        }

        return context.getString(this.stringResId);
    }

    public boolean isAppearanceLightStatusBars() {
        switch (this) {
            case BLACK:
                return false;
            case WHITE:
            case RED:
            case BLUE:
            case GREEN:
            case YELLOW:
                return true;
        }
        return true;
    }

    // TODO:colorResIdをコンストラクタで初期化するか検討(膨大な良の為可読性を考慮してこのままとしても良いかも)
    public int getPrimaryColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_primary;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_primary;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_primary;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_primary;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_primary;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_primary;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getOnPrimaryColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_onPrimary;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_onPrimary;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_onPrimary;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_onPrimary;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_onPrimary;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_onPrimary;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getPrimaryContainerColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_primaryContainer;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_primaryContainer;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_primaryContainer;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_primaryContainer;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_primaryContainer;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_primaryContainer;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getOnPrimaryContainerColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_onPrimaryContainer;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_onPrimaryContainer;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_onPrimaryContainer;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_onPrimaryContainer;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_onPrimaryContainer;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_onPrimaryContainer;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getSecondaryColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_secondary;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_secondary;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_secondary;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_secondary;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_secondary;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_secondary;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getOnSecondaryColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_onSecondary;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_onSecondary;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_onSecondary;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_onSecondary;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_onSecondary;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_onSecondary;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getSecondaryContainerColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_secondaryContainer;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_secondaryContainer;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_secondaryContainer;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_secondaryContainer;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_secondaryContainer;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_secondaryContainer;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getOnSecondaryContainerColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_onSecondaryContainer;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_onSecondaryContainer;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_onSecondaryContainer;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_onSecondaryContainer;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_onSecondaryContainer;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_onSecondaryContainer;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getTertiaryColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_tertiary;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_tertiary;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_tertiary;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_tertiary;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_tertiary;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_tertiary;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getOnTertiaryColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_onTertiary;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_onTertiary;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_onTertiary;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_onTertiary;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_onTertiary;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_onTertiary;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getTertiaryContainerColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_tertiaryContainer;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_tertiaryContainer;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_tertiaryContainer;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_tertiaryContainer;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_tertiaryContainer;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_tertiaryContainer;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getOnTertiaryContainerColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_onTertiaryContainer;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_onTertiaryContainer;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_onTertiaryContainer;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_onTertiaryContainer;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_onTertiaryContainer;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_onTertiaryContainer;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getSurfaceColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_surface;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_surface;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_surface;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_surface;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_surface;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_surface;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getSurfaceContainerLowColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_surfaceContainerLow;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_surfaceContainerLow;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_surfaceContainerLow;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_surfaceContainerLow;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_surfaceContainerLow;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_surfaceContainerLow;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getSurfaceContainerColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_surfaceContainer;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_surfaceContainer;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_surfaceContainer;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_surfaceContainer;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_surfaceContainer;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_surfaceContainer;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getSurfaceContainerHighColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_surfaceContainerHigh;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_surfaceContainerHigh;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_surfaceContainerHigh;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_surfaceContainerHigh;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_surfaceContainerHigh;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_surfaceContainerHigh;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getSurfaceContainerHighestColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_surfaceContainerHighest;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_surfaceContainerHighest;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_surfaceContainerHighest;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_surfaceContainerHighest;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_surfaceContainerHighest;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_surfaceContainerHighest;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getOnSurfaceColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_onSurface;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_onSurface;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_onSurface;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_onSurface;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_onSurface;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_onSurface;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getSurfaceDimColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_surfaceDim;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_surfaceDim;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_surfaceDim;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_surfaceDim;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_surfaceDim;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_surfaceDim;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public int getSurfaceVariantColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_surfaceVariant;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_surfaceVariant;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_surfaceVariant;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_surfaceVariant;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_surfaceVariant;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_surfaceVariant;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public int getOnSurfaceVariantColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_onSurfaceVariant;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_onSurfaceVariant;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_onSurfaceVariant;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_onSurfaceVariant;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_onSurfaceVariant;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_onSurfaceVariant;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getOutlineColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_outline;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_outline;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_outline;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_outline;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_outline;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_outline;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getOutlineVariantColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_outlineVariant;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_outlineVariant;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_outlineVariant;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_outlineVariant;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_outlineVariant;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_outlineVariant;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  int getErrorColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_color_white_error;
                break;
            case BLACK:
                colorResId = R.color.md_theme_color_black_error;
                break;
            case RED:
                colorResId = R.color.md_theme_color_red_error;
                break;
            case BLUE:
                colorResId = R.color.md_theme_color_blue_error;
                break;
            case GREEN:
                colorResId = R.color.md_theme_color_green_error;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_color_yellow_error;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public int getDatePickerDialogThemeResId() {
        switch (this) {
            case WHITE:
                return R.style.MaterialCalendarThemeColorWhite;
            case BLACK:
                return R.style.MaterialCalendarThemeColorBlack;
            case RED:
                return R.style.MaterialCalendarThemeColorRed;
            case BLUE:
                return R.style.MaterialCalendarThemeColorBlue;
            case GREEN:
                return R.style.MaterialCalendarThemeColorGreen;
            case YELLOW:
                return R.style.MaterialCalendarThemeColorYellow;
        }
        throw new IllegalStateException();
    }

    public int getThemeResId() {
        switch (this) {
            case WHITE:
                return R.style.AppThemeColorWhite;
            case BLACK:
                return R.style.AppThemeColorBlack;
            case RED:
                return R.style.AppThemeColorRed;
            case BLUE:
                return R.style.AppThemeColorBlue;
            case GREEN:
                return R.style.AppThemeColorGreen;
            case YELLOW:
                return R.style.AppThemeColorYellow;
        }
        throw new IllegalStateException();
    }

    /**
     * NumberPickerを含むBottomSheetDialogのViewをInflateする時のThemeResIdを取得する。
     * */
    public int getNumberPickerBottomSheetDialogThemeResId() {
        // HACK:下記理由から、ApiLevel29未満かつThemeColorBlackの時はNumberPickerBottomSheetDialogのThemeColorをWhiteにする。
        //      ・NumberPickerの値はThemeが適用されず、TextColorはApiLevel29以上からしか変更できない。
        //      ・ThemeColorBlackの時は背景が黒となり、NumberPickerの値が見えない。
        switch (this) {
            case WHITE:
                return R.style.AppThemeColorWhite;
            case BLACK:
                if (Build.VERSION.SDK_INT >= 29) {
                    return R.style.AppThemeColorBlack;
                } else {
                    return R.style.AppThemeColorWhite;
                }
            case RED:
                return R.style.AppThemeColorRed;
            case BLUE:
                return R.style.AppThemeColorBlue;
            case GREEN:
                return R.style.AppThemeColorGreen;
            case YELLOW:
                return R.style.AppThemeColorYellow;
        }
        throw new IllegalStateException();
    }

    public int getAlertDialogThemeResId() {
        switch (this) {
            case WHITE:
                return R.style.MaterialAlertDialogThemeColorWhite;
            case BLACK:
                return R.style.MaterialAlertDialogThemeColorBlack;
            case RED:
                return R.style.MaterialAlertDialogThemeColorRed;
            case BLUE:
                return R.style.MaterialAlertDialogThemeColorBlue;
            case GREEN:
                return R.style.MaterialAlertDialogThemeColorGreen;
            case YELLOW:
                return R.style.MaterialAlertDialogThemeColorYellow;
        }
        throw new IllegalStateException();
    }
}
