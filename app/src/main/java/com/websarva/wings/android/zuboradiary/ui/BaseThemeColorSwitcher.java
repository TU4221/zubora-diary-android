package com.websarva.wings.android.zuboradiary.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;

public class BaseThemeColorSwitcher {
    protected final Resources resources;
    protected final Context context;
    protected final ThemeColor themeColor;

    public BaseThemeColorSwitcher(Context context, ThemeColor themeColor) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (themeColor == null) {
            throw new NullPointerException();
        }

        resources = context.getResources();
        if (resources == null) {
            throw new NullPointerException();
        }

        this.context = context;
        this.themeColor = themeColor;
    }

    public final void switchStatusBarColor(Window window) {
        if (window == null) {
            throw new NullPointerException();
        }
        int surfaceColor = themeColor.getSurfaceColor(resources);
        window.setStatusBarColor(surfaceColor);

        boolean requests = themeColor.requestsSwitchingAppearanceLightStatusBars();
        new WindowInsetsControllerCompat(window, window.getDecorView()).setAppearanceLightStatusBars(requests);
    }

    public final void switchBackgroundColor(View view) {
        if (view == null) {
            throw new NullPointerException();
        }

        int surfaceColor = themeColor.getSurfaceColor(resources);
        ColorSwitchingViewList<View> viewList = new ColorSwitchingViewList<>(view);
        switchViewColor(surfaceColor, viewList);
    }

    public final void switchTextColorOnBackground(ColorSwitchingViewList<TextView> viewList) {
        if (viewList == null) {
            throw new NullPointerException();
        }

        int onSurfaceColor = themeColor.getOnSurfaceColor(resources);
        switchTextViewColorOnlyText(onSurfaceColor, viewList);
    }

    public final void switchToolbarColor(MaterialToolbar toolbar) {
        if (toolbar == null) {
            throw new NullPointerException();
        }

        int surfaceColor = themeColor.getSurfaceColor(resources);
        int onSurfaceColor = themeColor.getOnSurfaceColor(resources);
        toolbar.setBackgroundColor(surfaceColor);
        toolbar.setTitleTextColor(onSurfaceColor);

        Drawable navigationIcon = toolbar.getNavigationIcon();
        if (navigationIcon != null) {
            navigationIcon.setTint(onSurfaceColor);
        }

        Drawable menuIcon = toolbar.getOverflowIcon();
        if (menuIcon != null) {
            menuIcon.setTint(onSurfaceColor);
        }
    }

    public final void switchFloatingActionButtonColor(ColorSwitchingViewList<FloatingActionButton> fabList) {
        if (fabList == null) {
            throw new NullPointerException();
        }

        int primaryColor = themeColor.getPrimaryColor(resources);
        int onPrimaryColor = themeColor.getOnPrimaryColor(resources);
        fabList.getViewList().stream().forEach(x -> {
            x.setBackgroundTintList(ColorStateList.valueOf(primaryColor));
            x.setImageTintList(ColorStateList.valueOf(onPrimaryColor));
        });
    }

    public final void switchSwitchColor(ColorSwitchingViewList<MaterialSwitch> switchList) {
        if (switchList == null) {
            throw new NullPointerException();
        }

        int primaryColor = themeColor.getPrimaryColor(resources);
        int surfaceContainerHighestColor = themeColor.getSurfaceContainerHighestColor(resources);

        // 状態ごとの色を定義する
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},  // ONの状態
                new int[]{-android.R.attr.state_checked}  // OFFの状態
        };

        // 各状態に応じた色を定義する
        int[] colors = new int[]{
                primaryColor,  // ONの状態の色（例: 黒）
                surfaceContainerHighestColor   // OFFの状態の色（例: 白）
        };

        // カスタムColorStateListを作成する
        ColorStateList colorStateList = new ColorStateList(states, colors);

        switchList.getViewList().stream().forEach(x -> x.setTrackTintList(colorStateList));
    }

    public final void switchButton(ColorSwitchingViewList<Button> buttonList) {
        if (buttonList == null) {
            throw new NullPointerException();
        }

        int primaryColor = themeColor.getPrimaryColor(resources);
        int onPrimaryColor = themeColor.getOnPrimaryColor(resources);
        buttonList.getViewList().stream()
                .forEach(x -> {
                    x.setBackgroundColor(primaryColor);
                    x.setTextColor(onPrimaryColor);
                });
    }

    public final void switchImageButton(ColorSwitchingViewList<ImageButton> imageButtonList) {
        if (imageButtonList == null) {
            throw new NullPointerException();
        }

        int primaryColor = themeColor.getPrimaryColor(resources);
        imageButtonList.getViewList().stream()
                .forEach(x -> x.setImageTintList(ColorStateList.valueOf(primaryColor)));
    }

    // 共通処理
    protected final void switchViewColor(int color, ColorSwitchingViewList<View> viewList) {
        if (viewList == null) {
            throw new NullPointerException();
        }

        viewList.getViewList().stream().forEach(x -> x.setBackgroundColor(color));
    }

    protected final void switchTextViewColor(int color, int onColor, ColorSwitchingViewList<TextView> viewList) {
        if (viewList == null) {
            throw new NullPointerException();
        }

        viewList.getViewList().stream().forEach(x -> {
            x.setBackgroundColor(color);
            x.setTextColor(onColor);
        });
    }

    protected final void switchTextViewColorOnlyText(int onColor, ColorSwitchingViewList<TextView> viewList) {
        if (viewList == null) {
            throw new NullPointerException();
        }

        viewList.getViewList().stream().forEach(x -> x.setTextColor(onColor));
    }

    protected final void switchTextViewColorOnlyIcon(int color, ColorSwitchingViewList<TextView> textViewList) {
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
}
