package com.websarva.wings.android.zuboradiary.data.preferences;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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

    // TODO:colorResIdをコンストラクタで初期化するか検討(膨大な良の為可読性を考慮してこのままとしても良いかも)
    public int getPrimaryColor(Resources resources) {
        if (resources == null) {
            throw new NullPointerException();
        }

        int colorResId = -1;
        switch (this) {
            case WHITE:
                colorResId = R.color.md_theme_primary_white;
                break;
            case BLACK:
                colorResId = R.color.md_theme_primary_black;
                break;
            case RED:
                colorResId = R.color.md_theme_primary_black;
                break;
            case BLUE:
                colorResId = R.color.md_theme_primary_black;
                break;
            case GREEN:
                colorResId = R.color.md_theme_primary_black;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_primary_black;
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
                colorResId = R.color.md_theme_onPrimary_white;
                break;
            case BLACK:
                colorResId = R.color.md_theme_onPrimary_black;
                break;
            case RED:
                colorResId = R.color.md_theme_onPrimary_black;
                break;
            case BLUE:
                colorResId = R.color.md_theme_onPrimary_black;
                break;
            case GREEN:
                colorResId = R.color.md_theme_onPrimary_black;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_onPrimary_black;
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
                colorResId = R.color.md_theme_primaryContainer_white;
                break;
            case BLACK:
                colorResId = R.color.md_theme_primaryContainer_black;
                break;
            case RED:
                colorResId = R.color.md_theme_primaryContainer_black;
                break;
            case BLUE:
                colorResId = R.color.md_theme_primaryContainer_black;
                break;
            case GREEN:
                colorResId = R.color.md_theme_primaryContainer_black;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_primaryContainer_black;
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
                colorResId = R.color.md_theme_onPrimaryContainer_white;
                break;
            case BLACK:
                colorResId = R.color.md_theme_onPrimaryContainer_black;
                break;
            case RED:
                colorResId = R.color.md_theme_onPrimaryContainer_black;
                break;
            case BLUE:
                colorResId = R.color.md_theme_onPrimaryContainer_black;
                break;
            case GREEN:
                colorResId = R.color.md_theme_onPrimaryContainer_black;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_onPrimaryContainer_black;
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
                colorResId = R.color.md_theme_secondary_white;
                break;
            case BLACK:
                colorResId = R.color.md_theme_secondary_black;
                break;
            case RED:
                colorResId = R.color.md_theme_secondary_black;
                break;
            case BLUE:
                colorResId = R.color.md_theme_secondary_black;
                break;
            case GREEN:
                colorResId = R.color.md_theme_secondary_black;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_secondary_black;
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
                colorResId = R.color.md_theme_onSecondary_white;
                break;
            case BLACK:
                colorResId = R.color.md_theme_onSecondary_black;
                break;
            case RED:
                colorResId = R.color.md_theme_onSecondary_black;
                break;
            case BLUE:
                colorResId = R.color.md_theme_onSecondary_black;
                break;
            case GREEN:
                colorResId = R.color.md_theme_onSecondary_black;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_onSecondary_black;
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
                colorResId = R.color.md_theme_secondaryContainer_white;
                break;
            case BLACK:
                colorResId = R.color.md_theme_secondaryContainer_black;
                break;
            case RED:
                colorResId = R.color.md_theme_secondaryContainer_black;
                break;
            case BLUE:
                colorResId = R.color.md_theme_secondaryContainer_black;
                break;
            case GREEN:
                colorResId = R.color.md_theme_secondaryContainer_black;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_secondaryContainer_black;
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
                colorResId = R.color.md_theme_onSecondaryContainer_white;
                break;
            case BLACK:
                colorResId = R.color.md_theme_onSecondaryContainer_black;
                break;
            case RED:
                colorResId = R.color.md_theme_onSecondaryContainer_black;
                break;
            case BLUE:
                colorResId = R.color.md_theme_onSecondaryContainer_black;
                break;
            case GREEN:
                colorResId = R.color.md_theme_onSecondaryContainer_black;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_onSecondaryContainer_black;
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
                colorResId = R.color.md_theme_surface_white;
                break;
            case BLACK:
                colorResId = R.color.md_theme_surface_black;
                break;
            case RED:
                colorResId = R.color.md_theme_surface_black;
                break;
            case BLUE:
                colorResId = R.color.md_theme_surface_black;
                break;
            case GREEN:
                colorResId = R.color.md_theme_surface_black;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_surface_black;
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
                colorResId = R.color.md_theme_surfaceContainerLow_white;
                break;
            case BLACK:
                colorResId = R.color.md_theme_surfaceContainerLow_black;
                break;
            case RED:
                colorResId = R.color.md_theme_surfaceContainerLow_black;
                break;
            case BLUE:
                colorResId = R.color.md_theme_surfaceContainerLow_black;
                break;
            case GREEN:
                colorResId = R.color.md_theme_surfaceContainerLow_black;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_surfaceContainerLow_black;
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
                colorResId = R.color.md_theme_surfaceContainer_white;
                break;
            case BLACK:
                colorResId = R.color.md_theme_surfaceContainer_black;
                break;
            case RED:
                colorResId = R.color.md_theme_surfaceContainer_black;
                break;
            case BLUE:
                colorResId = R.color.md_theme_surfaceContainer_black;
                break;
            case GREEN:
                colorResId = R.color.md_theme_surfaceContainer_black;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_surfaceContainer_black;
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
                colorResId = R.color.md_theme_surfaceContainerHigh_white;
                break;
            case BLACK:
                colorResId = R.color.md_theme_surfaceContainerHigh_black;
                break;
            case RED:
                colorResId = R.color.md_theme_surfaceContainerHigh_black;
                break;
            case BLUE:
                colorResId = R.color.md_theme_surfaceContainerHigh_black;
                break;
            case GREEN:
                colorResId = R.color.md_theme_surfaceContainerHigh_black;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_surfaceContainerHigh_black;
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
                colorResId = R.color.md_theme_surfaceContainerHighest_white;
                break;
            case BLACK:
                colorResId = R.color.md_theme_surfaceContainerHighest_black;
                break;
            case RED:
                colorResId = R.color.md_theme_surfaceContainerHighest_black;
                break;
            case BLUE:
                colorResId = R.color.md_theme_surfaceContainerHighest_black;
                break;
            case GREEN:
                colorResId = R.color.md_theme_surfaceContainerHighest_black;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_surfaceContainerHighest_black;
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
                colorResId = R.color.md_theme_onSurface_white;
                break;
            case BLACK:
                colorResId = R.color.md_theme_onSurface_black;
                break;
            case RED:
                colorResId = R.color.md_theme_onSurface_black;
                break;
            case BLUE:
                colorResId = R.color.md_theme_onSurface_black;
                break;
            case GREEN:
                colorResId = R.color.md_theme_onSurface_black;
                break;
            case YELLOW:
                colorResId = R.color.md_theme_onSurface_black;
                break;
        }
        return ResourcesCompat.getColor(resources, colorResId, null);
    }

    public  ColorStateList getSwitchTrackDrawable(Context context) {
        if (context == null) {
            throw new NullPointerException();
        }

        int drawableResId = -1;
        switch (this) {
            case WHITE:
                drawableResId = R.color.switch_track_white;
                break;
            case BLACK:
                drawableResId = R.color.switch_track_black;
                break;
            case RED:
                drawableResId = R.color.switch_track_black;
                break;
            case BLUE:
                drawableResId = R.color.switch_track_black;
                break;
            case GREEN:
                drawableResId = R.color.switch_track_black;
                break;
            case YELLOW:
                drawableResId = R.color.switch_track_black;
                break;
        }
        return ContextCompat.getColorStateList(context, drawableResId);
    }
}
