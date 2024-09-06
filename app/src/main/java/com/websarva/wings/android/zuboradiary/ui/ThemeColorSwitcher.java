package com.websarva.wings.android.zuboradiary.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;

public class ThemeColorSwitcher {
    Resources resources;
    Context context;

    public ThemeColorSwitcher(Resources resources, Context context) {
        if (resources == null) {
            throw new NullPointerException();
        }
        if (context == null) {
            throw new NullPointerException();
        }

        this.resources = resources;
        this.context = context;
    }

    public void switchPrimaryColor(ThemeColor themeColor, ColorSwitchingViewList<View> viewList) {
        if (themeColor == null) {
            throw new NullPointerException();
        }

        int primaryColor = themeColor.getPrimaryColor(resources);
        int onPrimaryColor = themeColor.getOnPrimaryColor(resources);
        switchColor(primaryColor, onPrimaryColor, viewList);
    }

    public void switchSectionView(ThemeColor themeColor, ColorSwitchingViewList<TextView> textViewList) {
        if (themeColor == null) {
            throw new NullPointerException();
        }

        int surfaceContainerColor = themeColor.getSurfaceContainerColor(resources);
        int onSurfaceColor = themeColor.getOnSurfaceColor(resources);
        switchTextAndBackground(surfaceContainerColor, onSurfaceColor, textViewList);
    }

    public void switchTextIcon(ThemeColor themeColor, ColorSwitchingViewList<TextView> textViewList) {
        if (themeColor == null) {
            throw new NullPointerException();
        }

        int primaryColor = themeColor.getPrimaryColor(resources);
        switchTextIcon(primaryColor, textViewList);
    }

    public void switchSwitch(
            ThemeColor themeColor, ColorSwitchingViewList<MaterialSwitch> materialSwitchList) {
        if (themeColor == null) {
            throw new NullPointerException();
        }

        ColorStateList trackColorStateList = themeColor.getSwitchTrackDrawable(context);
        switchSwitch(trackColorStateList, materialSwitchList);
    }

    public void switchSurfaceColor(ThemeColor themeColor, ColorSwitchingViewList<View> viewList) {
        if (themeColor == null) {
            throw new NullPointerException();
        }

        int surfaceColor = themeColor.getSurfaceColor(resources);
        int onSurfaceColor = themeColor.getOnSurfaceColor(resources);
        switchColor(surfaceColor, onSurfaceColor, viewList);
    }

    public void switchSurfaceContainerLowColor(
            ThemeColor themeColor, ColorSwitchingViewList<View> viewList) {
        if (themeColor == null) {
            throw new NullPointerException();
        }

        int surfaceDimColor = themeColor.getSurfaceContainerLowColor(resources);
        int onSurfaceColor = themeColor.getOnSurfaceColor(resources);
        switchColor(surfaceDimColor, onSurfaceColor, viewList);
    }



    private void switchColor(int color,  int onColor, ColorSwitchingViewList<View> viewList) {
        if (viewList == null) {
            throw new NullPointerException();
        }

        for (View view: viewList.getViewList()) {
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
            int color,  int onColor, ColorSwitchingViewList<TextView> textViewList) {
        if (textViewList == null) {
            throw new NullPointerException();
        }

        for (TextView textView: textViewList.getViewList()) {
            textView.setBackgroundColor(color);
            textView.setTextColor(onColor);
        }
    }

    private void switchTextIcon(int color, ColorSwitchingViewList<TextView> textViewList) {
        if (textViewList == null) {
            throw new NullPointerException();
        }

        for (TextView textView: textViewList.getViewList()) {
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
            ColorStateList trackColorStateList, ColorSwitchingViewList<MaterialSwitch> materialSwitchList) {
        if (trackColorStateList == null) {
            throw new NullPointerException();
        }
        if (materialSwitchList == null) {
            throw new NullPointerException();
        }

        for (MaterialSwitch materialSwitch : materialSwitchList.getViewList()) {
            materialSwitch.setTrackTintList(trackColorStateList);
        }
    }

    private void switchBackGroundColor(int color, ColorSwitchingViewList<View> viewList) {
        if (viewList == null) {
            throw new NullPointerException();
        }

        for (View view: viewList.getViewList()) {
            view.setBackgroundColor(color);
        }
    }
}
