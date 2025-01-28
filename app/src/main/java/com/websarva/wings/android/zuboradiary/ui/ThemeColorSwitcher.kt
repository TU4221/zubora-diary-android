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

import java.util.List;
import java.util.Objects;

/**
 * Enum ThemeColorをもとにViewの色を変更するクラス。
 * Activity、各Fragment共通処理を本クラスにまとめる。
 * 各Fragment固有のViewに対しては本クラスを継承して継承クラスにメソッドを追加する。
 * 本クラスに記述されている各Viewの色はアプリ背景色(SurfaceColor)を考慮して選定。
 * */
public class ThemeColorSwitcher {
    protected final Resources resources;
    protected final Context context;
    protected final ThemeColor themeColor;

    public ThemeColorSwitcher(Context context, ThemeColor themeColor) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(themeColor);

        resources = context.getResources();
        Objects.requireNonNull(resources);

        this.context = context;
        this.themeColor = themeColor;
    }

    public final void switchBackgroundColor(View view) {
        Objects.requireNonNull(view);

        int surfaceColor = themeColor.getSurfaceColor(resources);
        switchViewColor(view, surfaceColor);
    }

    public final void switchTextColorOnBackground(List<TextView> textViewList) {
        Objects.requireNonNull(textViewList);
        textViewList.forEach(Objects::requireNonNull);

        int onSurfaceColor = themeColor.getOnSurfaceColor(resources);
        switchTextViewsColorOnlyText(textViewList, onSurfaceColor);
    }

    public final void switchRedTextColorOnBackground(List<TextView> textViewList) {
        Objects.requireNonNull(textViewList);
        textViewList.forEach(Objects::requireNonNull);

        int onSurfaceColor = themeColor.getErrorColor(resources);
        switchTextViewsColorOnlyText(textViewList, onSurfaceColor);
    }

    public final void switchStatusBarColor(Window window) {
        Objects.requireNonNull(window);

        int surfaceColor = themeColor.getSurfaceColor(resources);
        window.setStatusBarColor(surfaceColor);

        // ステータスバーのアイコンの色を変更(白 or 灰)
        boolean isLight = themeColor.isAppearanceLightStatusBars();
        new WindowInsetsControllerCompat(window, window.getDecorView()).setAppearanceLightStatusBars(isLight);
    }

    public final void switchBottomNavigationColor(BottomNavigationView bottomNavigationView) {
        Objects.requireNonNull(bottomNavigationView);

        switchBottomNavigationBackgroundColor(bottomNavigationView);
        switchBottomNavigationItemRippleColor(bottomNavigationView);
        switchBottomNavigationItemTextColor(bottomNavigationView);
        switchBottomNavigationItemIconColor(bottomNavigationView);
        switchBottomNavigationActiveIndicatorColor(bottomNavigationView);
    }

    private void switchBottomNavigationBackgroundColor (BottomNavigationView bottomNavigationView) {
        Objects.requireNonNull(bottomNavigationView);

        int color = themeColor.getSurfaceContainerColor(resources);
        bottomNavigationView.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private void switchBottomNavigationItemRippleColor(BottomNavigationView bottomNavigationView) {
        Objects.requireNonNull(bottomNavigationView);

        int checkedColor = themeColor.getPrimaryColor(resources);
        int unCheckedColor = themeColor.getOnSurfaceVariantColor(resources);
        ColorStateList colorStateList = createCheckedColorStateList(checkedColor, unCheckedColor);
        bottomNavigationView.setItemRippleColor(colorStateList);
    }

    private void switchBottomNavigationItemTextColor(BottomNavigationView bottomNavigationView) {
        Objects.requireNonNull(bottomNavigationView);

        int checkedColor = themeColor.getOnSurfaceColor(resources);
        int unCheckedColor = themeColor.getOnSurfaceVariantColor(resources);
        ColorStateList colorStateList = createCheckedColorStateList(checkedColor, unCheckedColor);
        bottomNavigationView.setItemTextColor(colorStateList);
    }

    private void switchBottomNavigationItemIconColor(BottomNavigationView bottomNavigationView) {
        Objects.requireNonNull(bottomNavigationView);

        int checkedColor = themeColor.getOnSecondaryContainerColor(resources);
        int unCheckedColor = themeColor.getOnSurfaceVariantColor(resources);
        ColorStateList colorStateList = createCheckedColorStateList(checkedColor, unCheckedColor);
        bottomNavigationView.setItemIconTintList(colorStateList);
    }

    private void switchBottomNavigationActiveIndicatorColor(BottomNavigationView bottomNavigationView) {
        Objects.requireNonNull(bottomNavigationView);

        int secondaryContainerColor = themeColor.getSecondaryContainerColor(resources);
        bottomNavigationView.setItemActiveIndicatorColor(ColorStateList.valueOf(secondaryContainerColor));
    }

    public final void switchToolbarColor(MaterialToolbar toolbar) {
        Objects.requireNonNull(toolbar);

        int surfaceColor = themeColor.getSurfaceColor(resources);
        int onSurfaceColor = themeColor.getOnSurfaceColor(resources);
        int onSurfaceVariantColor = themeColor.getOnSurfaceVariantColor(resources);
        toolbar.setBackgroundColor(surfaceColor);
        toolbar.setTitleTextColor(onSurfaceColor);
        switchToolbarMenuColor(toolbar, onSurfaceColor);
        switchToolbarNavigationIconColor(toolbar, onSurfaceVariantColor);
    }

    private void switchToolbarNavigationIconColor(MaterialToolbar toolbar, int color) {
        Objects.requireNonNull(toolbar);

        Drawable navigationIcon = toolbar.getNavigationIcon();
        if (navigationIcon == null) {
            return;
        }

        navigationIcon.setTint(color);
    }

    private void switchToolbarMenuColor(MaterialToolbar toolbar, int color) {
        Objects.requireNonNull(toolbar);

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
        Objects.requireNonNull(toolbar);

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

    public final void switchFloatingActionButtonColor(List<FloatingActionButton> fabList) {
        Objects.requireNonNull(fabList);
        fabList.forEach(Objects::requireNonNull);

        int primaryContainerColor = themeColor.getPrimaryContainerColor(resources);
        int onPrimaryContainerColor = themeColor.getOnPrimaryContainerColor(resources);
        fabList.forEach(x -> {
            x.setBackgroundTintList(ColorStateList.valueOf(primaryContainerColor));
            x.setImageTintList(ColorStateList.valueOf(onPrimaryContainerColor));
        });
    }

    public final void switchSwitchColor(List<MaterialSwitch> switchList) {
        Objects.requireNonNull(switchList);
        switchList.forEach(Objects::requireNonNull);

        switchSwitchThumbColor(switchList);
        switchSwitchThumbIconColor(switchList);
        switchSwitchTrackColor(switchList);
    }

    private void switchSwitchThumbColor(List<MaterialSwitch> switchList) {
        Objects.requireNonNull(switchList);
        switchList.forEach(Objects::requireNonNull);

        int checkedColor = themeColor.getOnPrimaryColor(resources);
        int unCheckedColor = themeColor.getOutlineColor(resources);
        ColorStateList thumbColorStateList = createCheckedColorStateList(checkedColor, unCheckedColor);
        switchList.forEach(x -> x.setThumbTintList(thumbColorStateList));
    }

    private void switchSwitchThumbIconColor(List<MaterialSwitch> switchList) {
        Objects.requireNonNull(switchList);
        switchList.forEach(Objects::requireNonNull);

        int checkedColor = themeColor.getOnPrimaryContainerColor(resources);
        int unCheckedColor = themeColor.getSurfaceContainerHighestColor(resources);
        ColorStateList thumbIconColorStateList = createCheckedColorStateList(checkedColor, unCheckedColor);
        switchList.forEach(x -> x.setThumbIconTintList(thumbIconColorStateList));
    }

    private void switchSwitchTrackColor(List<MaterialSwitch> switchList) {
        Objects.requireNonNull(switchList);
        switchList.forEach(Objects::requireNonNull);

        int checkedColor = themeColor.getPrimaryColor(resources);
        int unCheckedColor = themeColor.getSurfaceContainerHighestColor(resources);
        ColorStateList trackColorStateList = createCheckedColorStateList(checkedColor, unCheckedColor);
        switchList.forEach(x -> x.setTrackTintList(trackColorStateList));
    }

    public final void switchButtonColor(List<Button> buttonList) {
        Objects.requireNonNull(buttonList);
        buttonList.forEach(Objects::requireNonNull);

        int color = themeColor.getPrimaryColor(resources);
        int onColor = themeColor.getOnPrimaryColor(resources);
        buttonList.forEach(x -> {
            x.setBackgroundColor(color);
            x.setTextColor(onColor);
        });
    }

    public final void switchImageButtonColor(List<ImageButton> imageButtonList) {
        Objects.requireNonNull(imageButtonList);
        imageButtonList.forEach(Objects::requireNonNull);

        int color = themeColor.getPrimaryColor(resources);
        imageButtonList.forEach(x -> x.setImageTintList(ColorStateList.valueOf(color)));
    }

    public final void switchImageViewColor(List<ImageView> imageViewList) {
        Objects.requireNonNull(imageViewList);
        imageViewList.forEach(Objects::requireNonNull);

        int color = themeColor.getSecondaryColor(resources);
        imageViewList.forEach(x -> switchImageView(x, color));
    }

    public final void switchCircularProgressBarColor(ProgressBar progressBar) {
        Objects.requireNonNull(progressBar);

        int color = themeColor.getPrimaryContainerColor(resources);
        progressBar.getIndeterminateDrawable().setTint(color);
    }

    public final void switchDividerColor(List<MaterialDivider> dividerList) {
        Objects.requireNonNull(dividerList);
        dividerList.forEach(Objects::requireNonNull);

        int color = themeColor.getOutlineVariantColor(resources);
        dividerList.forEach(x -> x.setDividerColor(color));
    }

    // 共通処理
    protected final void switchViewsColor(List<View> viewList, int color) {
        Objects.requireNonNull(viewList);
        viewList.forEach(Objects::requireNonNull);

        viewList.forEach(x -> switchViewColor(x, color));
    }

    protected final void switchViewColor(View view, int color) {
        Objects.requireNonNull(view);

        view.setBackgroundColor(color);
    }

    protected final void switchTextViewsColor(List<TextView> textViewList, int color, int onColor) {
        Objects.requireNonNull(textViewList);
        textViewList.forEach(Objects::requireNonNull);

        textViewList.forEach(x -> switchTextViewColor(x, color, onColor));
    }

    protected final void switchTextViewColor(TextView textView, int color, int onColor) {
        Objects.requireNonNull(textView);

        textView.setBackgroundColor(color);
        textView.setTextColor(onColor);
    }

    protected final void switchTextViewsColorOnlyText(List<TextView> textViewList, int onColor) {
        Objects.requireNonNull(textViewList);
        textViewList.forEach(Objects::requireNonNull);

        textViewList.forEach(x -> switchTextViewColorOnlyText(x, onColor));
    }

    protected final void switchTextViewColorOnlyText(TextView textView, int color) {
        Objects.requireNonNull(textView);

        textView.setTextColor(color);
    }

    protected final void switchTextViewsColorOnlyIcon(List<TextView> textViewList, int color) {
        Objects.requireNonNull(textViewList);
        textViewList.forEach(Objects::requireNonNull);

        textViewList.forEach(x -> switchTextViewColorOnlyIcon(x, color));
    }

    protected final void switchTextViewColorOnlyIcon(TextView view, int color) {
        Objects.requireNonNull(view);

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
        Objects.requireNonNull(imageView);

        Drawable drawable = imageView.getDrawable();
        switchDrawableColor(drawable, color);
    }

    protected final void switchDrawableColor(Drawable drawable, int color) {
        Objects.requireNonNull(drawable);

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
