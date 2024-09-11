package com.websarva.wings.android.zuboradiary.ui.list;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

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

    public void switchCircularProgressBarColor(ColorSwitchingViewList<ProgressBar> progressBarList) {
        if (progressBarList == null) {
            throw new NullPointerException();
        }

        int primaryContainerColor = themeColor.getPrimaryContainerColor(resources);
        progressBarList.getViewList().stream()
                .forEach(x -> x.getIndeterminateDrawable().setTint(primaryContainerColor));
    }


}
