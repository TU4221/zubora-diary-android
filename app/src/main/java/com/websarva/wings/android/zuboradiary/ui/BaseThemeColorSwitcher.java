package com.websarva.wings.android.zuboradiary.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;

import dagger.internal.Preconditions;

/**
 * Enum ThemeColorをもとにViewの色を変更するクラス。
 * Activity、各Fragment共通処理を本クラスにまとめる。
 * 各Fragment固有のViewに対しては本クラスを継承して継承クラスにメソッドを追加する。
 * 本クラスに記述されている各Viewの色はアプリ背景色(SurfaceColor)を考慮して選定。
 * */
public class BaseThemeColorSwitcher {
    protected final Resources resources;
    protected final Context context;
    protected final ThemeColor themeColor;

    public BaseThemeColorSwitcher(Context context, ThemeColor themeColor) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(themeColor);

        resources = context.getResources();
        Preconditions.checkNotNull(resources);

        this.context = context;
        this.themeColor = themeColor;
    }

    public final void switchBackgroundColor(View view) {
        Preconditions.checkNotNull(view);

        int surfaceColor = themeColor.getSurfaceColor(resources);
        switchViewColor(view, surfaceColor);
    }

    public final void switchTextColorOnBackground(ColorSwitchingViewList<TextView> viewList) {
        Preconditions.checkNotNull(viewList);

        int onSurfaceColor = themeColor.getOnSurfaceColor(resources);
        switchTextViewsColorOnlyText(viewList, onSurfaceColor);
    }

    public final void switchStatusBarColor(Window window) {
        Preconditions.checkNotNull(window);

        int surfaceColor = themeColor.getSurfaceColor(resources);
        window.setStatusBarColor(surfaceColor);

        // ステータスバーのアイコンの色を変更(白 or 灰)
        boolean requests = themeColor.requestsSwitchingAppearanceLightStatusBars();
        new WindowInsetsControllerCompat(window, window.getDecorView()).setAppearanceLightStatusBars(requests);
    }

    // TODO:ThemeResIdが設定できないためサイトで確認しながら個々設定できるようにする
    public final void switchBottomNavigationColor(BottomNavigationView view) {
        Preconditions.checkNotNull(view);

        switchBottomNavigationBackgroundColor(view);
        switchBottomNavigationItemRippleColor(view);
        switchBottomNavigationItemTextColor(view);
        switchBottomNavigationItemIconColor(view);
        switchBottomNavigationActiveIndicatorColor(view);
    }

    private void switchBottomNavigationBackgroundColor (BottomNavigationView view) {
        Preconditions.checkNotNull(view);

        int surfaceContainerColor = themeColor.getSurfaceContainerColor(resources);
        view.setBackgroundTintList(ColorStateList.valueOf(surfaceContainerColor));
    }

    private void switchBottomNavigationItemRippleColor(BottomNavigationView view) {
        Preconditions.checkNotNull(view);

        int checkedColor = themeColor.getPrimaryColor(resources);
        int unCheckedColor = themeColor.getOnSurfaceVariantColor(resources);
        ColorStateList colorStateList = createCheckedColorStateList(checkedColor, unCheckedColor);
        view.setItemRippleColor(colorStateList);
    }

    private void switchBottomNavigationItemTextColor(BottomNavigationView view) {
        Preconditions.checkNotNull(view);

        int checkedColor = themeColor.getOnSurfaceColor(resources);
        int unCheckedColor = themeColor.getOnSurfaceVariantColor(resources);
        ColorStateList colorStateList = createCheckedColorStateList(checkedColor, unCheckedColor);
        view.setItemTextColor(colorStateList);
    }

    private void switchBottomNavigationItemIconColor(BottomNavigationView view) {
        Preconditions.checkNotNull(view);

        int checkedColor = themeColor.getOnSecondaryContainerColor(resources);
        int unCheckedColor = themeColor.getOnSurfaceVariantColor(resources);
        ColorStateList colorStateList = createCheckedColorStateList(checkedColor, unCheckedColor);
        view.setItemIconTintList(colorStateList);
    }

    private void switchBottomNavigationActiveIndicatorColor(BottomNavigationView view) {
        Preconditions.checkNotNull(view);

        int secondaryContainerColor = themeColor.getSecondaryContainerColor(resources);
        view.setItemActiveIndicatorColor(ColorStateList.valueOf(secondaryContainerColor));
    }

    public final void switchToolbarColor(MaterialToolbar toolbar) {
        Preconditions.checkNotNull(toolbar);

        int surfaceColor = themeColor.getSurfaceColor(resources);
        int onSurfaceColor = themeColor.getOnSurfaceColor(resources);
        int onSurfaceVariantColor = themeColor.getOnSurfaceVariantColor(resources);
        toolbar.setBackgroundColor(surfaceColor);
        toolbar.setTitleTextColor(onSurfaceColor);
        switchToolbarMenuColor(toolbar, onSurfaceColor);
        switchToolbarNavigationIconColor(toolbar, onSurfaceVariantColor);
    }

    private void switchToolbarNavigationIconColor(MaterialToolbar toolbar, int color) {
        Preconditions.checkNotNull(toolbar);

        Drawable navigationIcon = toolbar.getNavigationIcon();
        if (navigationIcon == null) {
            return;
        }

        navigationIcon.setTint(color);
    }

    private void switchToolbarMenuColor(MaterialToolbar toolbar, int color) {
        Preconditions.checkNotNull(toolbar);

        Drawable menuIcon = toolbar.getOverflowIcon();
        if (menuIcon != null) {
            menuIcon.setTint(color);
        }

        Drawable collapseIcon = toolbar.getCollapseIcon();
        if (collapseIcon != null) {
            collapseIcon.setTint(color);
        }

        switchToolbarMenuIconColor(toolbar, color);
    }

    private void switchToolbarMenuIconColor(MaterialToolbar toolbar, int color) {
        Preconditions.checkNotNull(toolbar);

        Menu menu = toolbar.getMenu();
        if (menu == null) {
            return;
        }

        int numMenuIcons = menu.size();
        if (numMenuIcons <= 0) {
            return;
        }

        for (int i = 0; i < numMenuIcons; i++) {
            Drawable icon = menu.getItem(i).getIcon();
            if (icon != null) {
                icon.setTint(color);
            }
        }
    }

    public final void switchFloatingActionButtonColor(ColorSwitchingViewList<FloatingActionButton> fabList) {
        Preconditions.checkNotNull(fabList);

        int primaryContainerColor = themeColor.getPrimaryContainerColor(resources);
        int onPrimaryContainerColor = themeColor.getOnPrimaryContainerColor(resources);
        fabList.getViewList().stream().forEach(x -> {
            x.setBackgroundTintList(ColorStateList.valueOf(primaryContainerColor));
            x.setImageTintList(ColorStateList.valueOf(onPrimaryContainerColor));
        });
    }

    public final void switchSwitchColor(ColorSwitchingViewList<MaterialSwitch> switchList) {
        Preconditions.checkNotNull(switchList);

        switchSwitchThumbColor(switchList);
        switchSwitchThumbIconColor(switchList);
        switchSwitchTrackColor(switchList);
    }

    private void switchSwitchThumbColor(ColorSwitchingViewList<MaterialSwitch> switchList) {
        Preconditions.checkNotNull(switchList);

        int checkedColor = themeColor.getOnPrimaryColor(resources);
        int unCheckedColor = themeColor.getOutlineColor(resources);
        ColorStateList thumbColorStateList = createCheckedColorStateList(checkedColor, unCheckedColor);
        switchList.getViewList().stream().forEach(x -> x.setThumbTintList(thumbColorStateList));
    }

    private void switchSwitchThumbIconColor(ColorSwitchingViewList<MaterialSwitch> switchList) {
        Preconditions.checkNotNull(switchList);

        int checkedColor = themeColor.getOnPrimaryContainerColor(resources);
        int unCheckedColor = themeColor.getSurfaceContainerHighestColor(resources);
        ColorStateList thumbIconColorStateList = createCheckedColorStateList(checkedColor, unCheckedColor);
        switchList.getViewList().stream().forEach(x -> x.setThumbIconTintList(thumbIconColorStateList));
    }

    private void switchSwitchTrackColor(ColorSwitchingViewList<MaterialSwitch> switchList) {
        Preconditions.checkNotNull(switchList);

        int checkedColor = themeColor.getPrimaryColor(resources);
        int unCheckedColor = themeColor.getSurfaceContainerHighestColor(resources);
        ColorStateList trackColorStateList = createCheckedColorStateList(checkedColor, unCheckedColor);
        switchList.getViewList().stream().forEach(x -> x.setTrackTintList(trackColorStateList));
    }

    public final void switchButtonColor(ColorSwitchingViewList<Button> buttonList) {
        Preconditions.checkNotNull(buttonList);

        int color = themeColor.getPrimaryColor(resources);
        int onColor = themeColor.getOnPrimaryColor(resources);
        buttonList.getViewList().stream()
                .forEach(x -> {
                    x.setBackgroundColor(color);
                    x.setTextColor(onColor);
                });
    }

    public final void switchImageButtonColor(ColorSwitchingViewList<ImageButton> imageButtonList) {
        Preconditions.checkNotNull(imageButtonList);

        int color = themeColor.getPrimaryColor(resources);
        imageButtonList.getViewList().stream()
                .forEach(x -> x.setImageTintList(ColorStateList.valueOf(color)));
    }

    public void switchImageViewColor(ColorSwitchingViewList<ImageView> imageViewList) {
        Preconditions.checkNotNull(imageViewList);

        int color = themeColor.getSecondaryColor(resources);
        imageViewList.getViewList().stream().forEach(x -> switchImageView(x, color));
    }

    public void switchCircularProgressBarColor(ProgressBar progressBar) {
        Preconditions.checkNotNull(progressBar);

        int color = themeColor.getPrimaryContainerColor(resources);
        progressBar.getIndeterminateDrawable().setTint(color);
    }

    public void switchDividerColor(ColorSwitchingViewList<MaterialDivider> divider) {
        Preconditions.checkNotNull(divider);

        int color = themeColor.getOutlineVariantColor(resources);
        divider.getViewList().stream().forEach(x -> x.setDividerColor(color));
    }

    // 共通処理
    protected final void switchViewsColor(ColorSwitchingViewList<View> viewList, int color) {
        Preconditions.checkNotNull(viewList);

        viewList.getViewList().stream().forEach(x -> switchViewColor(x, color));
    }

    protected final void switchViewColor(View view, int color) {
        Preconditions.checkNotNull(view);

        view.setBackgroundColor(color);
    }

    protected final void switchTextViewsColor(ColorSwitchingViewList<TextView> viewList, int color, int onColor) {
        Preconditions.checkNotNull(viewList);

        viewList.getViewList().stream().forEach(x -> switchTextViewColor(x, color, onColor));
    }

    protected final void switchTextViewColor(TextView view, int color, int onColor) {
        Preconditions.checkNotNull(view);

        view.setBackgroundColor(color);
        view.setTextColor(onColor);
    }

    protected final void switchTextViewsColorOnlyText(ColorSwitchingViewList<TextView> viewList, int onColor) {
        Preconditions.checkNotNull(viewList);

        viewList.getViewList().stream().forEach(x -> switchTextViewColorOnlyText(x, onColor));
    }

    protected final void switchTextViewColorOnlyText(TextView view, int color) {
        Preconditions.checkNotNull(view);

        view.setTextColor(color);
    }

    protected final void switchTextViewsColorOnlyIcon(ColorSwitchingViewList<TextView> viewList, int color) {
        Preconditions.checkNotNull(viewList);

        viewList.getViewList().stream().forEach(x -> switchTextViewColorOnlyIcon(x, color));
    }

    protected final void switchTextViewColorOnlyIcon(TextView view, int color) {
        Preconditions.checkNotNull(view);

        Drawable[] drawables = view.getCompoundDrawablesRelative();
        Drawable[] wrappedDrawable = new Drawable[drawables.length];

        for (int i = 0; i < drawables.length; i++) {
            Drawable drawable = drawables[i];
            if (drawable != null) {
                wrappedDrawable[i] = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(wrappedDrawable[i], color);
            }
        }
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(
                wrappedDrawable[0], wrappedDrawable[1], wrappedDrawable[2], wrappedDrawable[3]);
    }

    protected final void switchImageView(ImageView imageView, int color) {
        Preconditions.checkNotNull(imageView);

        Drawable drawable = imageView.getDrawable();
        switchDrawableColor(drawable, color);
    }

    protected final void switchDrawableColor(Drawable drawable, int color) {
        Preconditions.checkNotNull(drawable);

        drawable.setTint(color);
    }

    @NonNull
    protected final ColorStateList createCheckedColorStateList(int checkedColor, int unCheckedColor) {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked },  // ON状態
                new int[] { -android.R.attr.state_checked }  // OFF状態
        };
        int[] colors = new int[] {
                checkedColor,
                unCheckedColor
        };

        return new ColorStateList(states, colors);
    }
}
