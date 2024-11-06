package com.websarva.wings.android.zuboradiary.ui.settings;

import android.content.Context;
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.ThemeColorSwitcher;
import com.websarva.wings.android.zuboradiary.ui.ColorSwitchingViewList;

import java.util.Objects;

class SettingsThemeColorSwitcher extends ThemeColorSwitcher {
    SettingsThemeColorSwitcher(Context context, ThemeColor themeColor) {
        super(context, themeColor);
    }

    void switchSettingItemSectionColor(ColorSwitchingViewList<TextView> textViewList) {
        Objects.requireNonNull(textViewList);

        int color = themeColor.getSecondaryContainerColor(resources);
        int onColor = themeColor.getOnSecondaryContainerColor(resources);
        switchTextViewsColor(textViewList, color, onColor);
    }

    void switchSettingItemIconColor(ColorSwitchingViewList<TextView> textViewList) {
        Objects.requireNonNull(textViewList);

        int color = themeColor.getOnSurfaceVariantColor(resources);
        switchTextViewsColorOnlyIcon(textViewList, color);
    }
}
