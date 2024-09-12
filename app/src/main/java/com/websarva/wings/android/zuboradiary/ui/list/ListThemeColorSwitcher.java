package com.websarva.wings.android.zuboradiary.ui.list;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.BaseThemeColorSwitcher;
import com.websarva.wings.android.zuboradiary.ui.ColorSwitchingViewList;

public class ListThemeColorSwitcher extends BaseThemeColorSwitcher {
    public ListThemeColorSwitcher(Context context, ThemeColor themeColor) {
        super(context, themeColor);
    }

    public void switchListSectionBarColor(ColorSwitchingViewList<TextView> viewList) {
        if (viewList == null) {
            throw new NullPointerException();
        }

        int secondaryColor = themeColor.getSecondaryColor(resources);
        int onSecondaryColor = themeColor.getOnSecondaryColor(resources);
        switchTextViewColor(secondaryColor, onSecondaryColor, viewList);
    }

    public void switchListItemBackgroundColor(View recyclerBackgroundView) {
        if (recyclerBackgroundView == null) {
            throw new NullPointerException();
        }

        int secondaryContainerColor = themeColor.getSecondaryContainerColor(resources);
        ColorSwitchingViewList<View> recyclerBackgroundViewList = new ColorSwitchingViewList<>(recyclerBackgroundView);
        switchViewColor(secondaryContainerColor, recyclerBackgroundViewList);
    }

    public void switchTextColorOnListItemBackground(ColorSwitchingViewList<TextView> textViewList) {
        if (textViewList == null) {
            throw new NullPointerException();
        }

        int onSecondaryContainerColor = themeColor.getOnSecondaryContainerColor(resources);
        switchTextViewColorOnlyText(onSecondaryContainerColor, textViewList);
    }

    public void switchImageViewColorOnListItemBackground(ImageView imageView) {
        if (imageView == null) {
            throw new NullPointerException();
        }

        int secondaryColor = themeColor.getSecondaryColor(resources);
        Drawable drawable = imageView.getDrawable();
        drawable.setTint(secondaryColor);
        imageView.setImageDrawable(drawable);
    }

    public void switchCircularProgressBarColor(ColorSwitchingViewList<ProgressBar> progressBarList) {
        if (progressBarList == null) {
            throw new NullPointerException();
        }

        int primaryContainerColor = themeColor.getPrimaryContainerColor(resources);
        progressBarList.getViewList().stream()
                .forEach(x -> x.getIndeterminateDrawable().setTint(primaryContainerColor));
    }

    public void switchKeyWordSearchBackgroundColor(View background) {
        if (background == null) {
            throw new NullPointerException();
        }

        int surfaceDimColor = themeColor.getSurfaceDimColor(resources);
        background.setBackgroundTintList(ColorStateList.valueOf(surfaceDimColor));
    }

    public void switchKeyWordSearchTextColor(TextView textView) {
        if (textView == null) {
            throw new NullPointerException();
        }

        int onSurfaceColor = themeColor.getOnSurfaceColor(resources);
        ColorSwitchingViewList<TextView> textViewList = new ColorSwitchingViewList<>(textView);
        switchTextViewColorOnlyText(onSurfaceColor, textViewList);
    }

}
