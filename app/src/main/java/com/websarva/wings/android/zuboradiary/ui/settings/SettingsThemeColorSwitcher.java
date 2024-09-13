package com.websarva.wings.android.zuboradiary.ui.settings;

import android.content.Context;
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.BaseThemeColorSwitcher;
import com.websarva.wings.android.zuboradiary.ui.ColorSwitchingViewList;

import dagger.internal.Preconditions;

class SettingsThemeColorSwitcher extends BaseThemeColorSwitcher {
    SettingsThemeColorSwitcher(Context context, ThemeColor themeColor) {
        super(context, themeColor);
    }

    void switchSettingItemSectionColor(ColorSwitchingViewList<TextView> textViewList) {
        Preconditions.checkNotNull(textViewList);

        int color = themeColor.getSurfaceContainerColor(resources);
        int onColor = themeColor.getOnSurfaceColor(resources);
        switchTextViewsColor(textViewList, color, onColor);
    }

    void switchSettingItemIconColor(ColorSwitchingViewList<TextView> textViewList) {
        Preconditions.checkNotNull(textViewList);

        int color = themeColor.getPrimaryColor(resources);
        switchTextViewsColorOnlyIcon(textViewList, color);
    }
}
