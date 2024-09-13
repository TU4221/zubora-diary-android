package com.websarva.wings.android.zuboradiary.ui.diary;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.BaseThemeColorSwitcher;

import dagger.internal.Preconditions;

public class DiaryThemeColorSwitcher extends BaseThemeColorSwitcher {
    public DiaryThemeColorSwitcher(Context context, ThemeColor themeColor) {
        super(context, themeColor);
    }

    public void switchSpinnerDropDownTextColor(TextView textView) {
        Preconditions.checkNotNull(textView);

        int color = themeColor.getSurfaceColor(resources);
        int onColor = themeColor.getOnSurfaceColor(resources);
        switchTextViewColor(textView, color, onColor);
    }

    public void switchAttachedPictureImageViewColor(ImageView imageView) {
        Preconditions.checkNotNull(imageView);

        int color = themeColor.getSecondaryContainerColor(resources);
        switchImageView(imageView, color);

        Drawable background = imageView.getBackground();
        if (background instanceof GradientDrawable) {
            GradientDrawable _background = (GradientDrawable) background;
            _background.setStroke(2, color);
            imageView.setBackground(_background);
        } else {
            throw new ClassCastException();
        }
    }

    public void switchHistoryItemTextColor(TextView textView) {
        Preconditions.checkNotNull(textView);

        int color = themeColor.getSurfaceColor(resources);
        int onColor = themeColor.getOnSurfaceColor(resources);
        switchTextViewColor(textView, color, onColor);
    }
}
