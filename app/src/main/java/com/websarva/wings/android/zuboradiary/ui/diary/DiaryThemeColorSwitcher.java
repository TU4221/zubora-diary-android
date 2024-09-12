package com.websarva.wings.android.zuboradiary.ui.diary;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.BaseThemeColorSwitcher;
import com.websarva.wings.android.zuboradiary.ui.ColorSwitchingViewList;

public class DiaryThemeColorSwitcher extends BaseThemeColorSwitcher {
    public DiaryThemeColorSwitcher(Context context, ThemeColor themeColor) {
        super(context, themeColor);
    }

    public void switchSpinnerDropDownTextColor(TextView textView) {
        if (textView == null) {
            throw new NullPointerException();
        }

        int surfaceColor = themeColor.getSurfaceColor(resources);
        int onSurfaceColor = themeColor.getOnSurfaceColor(resources);
        ColorSwitchingViewList<TextView> textViewList = new ColorSwitchingViewList<>(textView);
        switchTextViewColor(surfaceColor, onSurfaceColor, textViewList);
    }

    public void switchAttachedPictureImageViewColor(ImageView imageView) {
        if (imageView == null) {
            throw new NullPointerException();
        }

        int secondaryContainerColor = themeColor.getSecondaryContainerColor(resources);
        Drawable drawable = imageView.getDrawable();
        drawable.setTint(secondaryContainerColor);
        imageView.setImageDrawable(drawable);

        Drawable background = imageView.getBackground();
        if (background instanceof GradientDrawable) {
            GradientDrawable _background = (GradientDrawable) background;
            _background.setStroke(2, secondaryContainerColor);
            imageView.setBackground(_background);
        } else {
            throw new ClassCastException();
        }
    }

    public void switchHistoryItemTextColor(TextView recyclerItemView) {
        if (recyclerItemView == null) {
            throw new NullPointerException();
        }

        int surfaceColor = themeColor.getSurfaceColor(resources);
        int onSurfaceColor = themeColor.getOnSurfaceColor(resources);
        ColorSwitchingViewList<TextView> recyclerItemViewList = new ColorSwitchingViewList<>(recyclerItemView);
        switchTextViewColor(surfaceColor, onSurfaceColor, recyclerItemViewList);
    }
}
