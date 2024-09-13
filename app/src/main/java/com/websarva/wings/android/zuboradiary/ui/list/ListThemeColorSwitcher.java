package com.websarva.wings.android.zuboradiary.ui.list;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.BaseThemeColorSwitcher;
import com.websarva.wings.android.zuboradiary.ui.ColorSwitchingViewList;

import dagger.internal.Preconditions;

public class ListThemeColorSwitcher extends BaseThemeColorSwitcher {
    public ListThemeColorSwitcher(Context context, ThemeColor themeColor) {
        super(context, themeColor);
    }

    public void switchListSectionBarColor(TextView textView) {
        Preconditions.checkNotNull(textView);

        int color = themeColor.getSecondaryColor(resources);
        int onColor = themeColor.getOnSecondaryColor(resources);
        switchTextViewColor(textView, color, onColor);
    }

    public void switchListItemBackgroundColor(View view) {
        Preconditions.checkNotNull(view);

        int color = themeColor.getSecondaryContainerColor(resources);
        switchViewColor(view, color);
    }

    public void switchTextColorOnListItemBackground(ColorSwitchingViewList<TextView> textViewList) {
        Preconditions.checkNotNull(textViewList);

        int color = themeColor.getOnSecondaryContainerColor(resources);
        switchTextViewsColorOnlyText(textViewList, color);
    }

    public void switchImageViewColorOnListItemBackground(ImageView imageView) {
        Preconditions.checkNotNull(imageView);

        int color = themeColor.getSecondaryColor(resources);
        switchImageView(imageView, color);
    }

    public void switchKeyWordSearchBackgroundColor(View view) {
        Preconditions.checkNotNull(view);

        int color = themeColor.getSurfaceDimColor(resources);
        Drawable background = view.getBackground();
        switchDrawableColor(background, color);
    }

    public void switchKeyWordSearchTextColor(TextView textView) {
        Preconditions.checkNotNull(textView);

        int color = themeColor.getOnSurfaceColor(resources);
        switchTextViewColorOnlyText(textView, color);
    }

}
