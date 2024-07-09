package com.websarva.wings.android.zuboradiary.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.websarva.wings.android.zuboradiary.R;
import com.websarva.wings.android.zuboradiary.data.settings.ThemeColors;

public class ThemeColorSwitcher {
    Resources resources;
    Context context;

    public ThemeColorSwitcher(@NonNull Resources resources, Context context) {
        this.resources = resources;
        this.context = context;
    }

    public void switchPrimaryColor(@NonNull ThemeColors themeColor,@NonNull View... views) {
        int primaryColor = getPrimaryColor(resources, themeColor);
        int onPrimaryColor = getOnPrimaryColor(resources, themeColor);
        switchColor(primaryColor, onPrimaryColor, views);
    }

    public void switchSectionView(@NonNull ThemeColors themeColor,@NonNull TextView... textViews) {
        int surfaceContainerColor = getSurfaceContainerColor(resources, themeColor);
        int onSurfaceColor = getOnSurfaceColor(resources, themeColor);
        switchTextAndBackground(surfaceContainerColor, onSurfaceColor, textViews);
    }

    public void switchTextIcon(@NonNull ThemeColors themeColor,@NonNull TextView... textViews) {
        int primaryColor = getPrimaryColor(resources, themeColor);
        switchTextIcon(primaryColor, textViews);
    }

    public void switchSwitch(@NonNull ThemeColors themeColor,@NonNull MaterialSwitch... materialSwitches) {
        ColorStateList trackColorStateList = getSwitchTrackDrawable(context, themeColor);
        switchSwitch(trackColorStateList, materialSwitches);
    }

    public void switchSurfaceColor(@NonNull ThemeColors themeColor,@NonNull View... views) {
        int surfaceColor = getSurfaceColor(resources, themeColor);
        int onSurfaceColor = getOnSurfaceColor(resources, themeColor);
        switchColor(surfaceColor, onSurfaceColor, views);
    }

    public void switchSurfaceContainerLowColor(@NonNull ThemeColors themeColor, @NonNull View... views) {
        int surfaceDimColor = getSurfaceContainerLowColor(resources, themeColor);
        int onSurfaceColor = getOnSurfaceColor(resources, themeColor);
        switchColor(surfaceDimColor, onSurfaceColor, views);
    }

    private int getPrimaryColor(@NonNull Resources resources,@NonNull ThemeColors themeColor) {
        int colorResId = -1;
        switch (themeColor) {
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

    private int getOnPrimaryColor(@NonNull Resources resources,@NonNull ThemeColors themeColor) {
        int colorResId = -1;
        switch (themeColor) {
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

    private int getPrimaryContainerColor(@NonNull Resources resources,@NonNull ThemeColors themeColor) {
        int colorResId = -1;
        switch (themeColor) {
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

    private int getOnPrimaryContainerColor(@NonNull Resources resources,@NonNull ThemeColors themeColor) {
        int colorResId = -1;
        switch (themeColor) {
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

    private int getSecondaryColor(@NonNull Resources resources,@NonNull ThemeColors themeColor) {
        int colorResId = -1;
        switch (themeColor) {
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

    private int getOnSecondaryColor(@NonNull Resources resources,@NonNull ThemeColors themeColor) {
        int colorResId = -1;
        switch (themeColor) {
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

    private int getSecondaryContainerColor(@NonNull Resources resources,@NonNull ThemeColors themeColor) {
        int colorResId = -1;
        switch (themeColor) {
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

    private int getOnSecondaryContainerColor(@NonNull Resources resources,@NonNull ThemeColors themeColor) {
        int colorResId = -1;
        switch (themeColor) {
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

    private int getSurfaceColor(@NonNull Resources resources,@NonNull ThemeColors themeColor) {
        int colorResId = -1;
        switch (themeColor) {
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

    private int getSurfaceContainerLowColor(@NonNull Resources resources, @NonNull ThemeColors themeColor) {
        int colorResId = -1;
        switch (themeColor) {
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

    private int getSurfaceContainerColor(@NonNull Resources resources, @NonNull ThemeColors themeColor) {
        int colorResId = -1;
        switch (themeColor) {
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

    private int getSurfaceContainerHighColor(@NonNull Resources resources, @NonNull ThemeColors themeColor) {
        int colorResId = -1;
        switch (themeColor) {
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

    private int getSurfaceContainerHighestColor(@NonNull Resources resources, @NonNull ThemeColors themeColor) {
        int colorResId = -1;
        switch (themeColor) {
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

    private int getOnSurfaceColor(@NonNull Resources resources,@NonNull ThemeColors themeColor) {
        int colorResId = -1;
        switch (themeColor) {
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

    private ColorStateList getSwitchTrackDrawable(@NonNull Context context,@NonNull ThemeColors themeColor) {
        int drawableResId = -1;
        switch (themeColor) {
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

    private void switchColor(@NonNull int color,  @NonNull int onColor, @NonNull View... views) {
        for (View view: views) {
            view.setBackgroundColor(color);
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                textView.setTextColor(onColor);

                Drawable[] drawables = textView.getCompoundDrawablesRelative();
                Drawable[] wrappedDrawable = new Drawable[drawables.length];
                for (int i = 0; i < drawables.length; i++) {
                    Drawable drawable = drawables[i];
                    if (drawable != null) {
                        wrappedDrawable[i] = DrawableCompat.wrap(drawable);
                        DrawableCompat.setTint(wrappedDrawable[i], onColor);
                    }
                }
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        wrappedDrawable[0], wrappedDrawable[1], wrappedDrawable[2], wrappedDrawable[3]);

            }
        }
    }

    private void switchTextAndBackground(
            @NonNull int color,  @NonNull int onColor, @NonNull TextView... textViews) {
        for (TextView textView: textViews) {
            textView.setBackgroundColor(color);
            textView.setTextColor(onColor);
        }
    }

    private void switchTextIcon(
            @NonNull int color, @NonNull TextView... textViews) {
        for (TextView textView: textViews) {
            Drawable[] drawables = textView.getCompoundDrawablesRelative();
            Drawable[] wrappedDrawable = new Drawable[drawables.length];
            for (int i = 0; i < drawables.length; i++) {
                Drawable drawable = drawables[i];
                if (drawable != null) {
                    wrappedDrawable[i] = DrawableCompat.wrap(drawable);
                    DrawableCompat.setTint(wrappedDrawable[i], color);
                }
            }
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    wrappedDrawable[0], wrappedDrawable[1], wrappedDrawable[2], wrappedDrawable[3]);
        }
    }

    private void switchSwitch(
            @NonNull ColorStateList trackColorStateList, @NonNull MaterialSwitch... materialSwitches) {
        for (MaterialSwitch materialSwitch : materialSwitches) {
            materialSwitch.setTrackTintList(trackColorStateList);
        }
    }

    private void switchBackGroundColor(@NonNull int color, @NonNull View... views) {
        for (View view: views) {
            view.setBackgroundColor(color);
        }
    }
}
