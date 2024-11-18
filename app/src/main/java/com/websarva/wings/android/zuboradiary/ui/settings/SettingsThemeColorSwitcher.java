package com.websarva.wings.android.zuboradiary.ui.settings;

import android.content.Context;
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.ThemeColorSwitcher;

import java.util.List;
import java.util.Objects;

class SettingsThemeColorSwitcher extends ThemeColorSwitcher {
    SettingsThemeColorSwitcher(Context context, ThemeColor themeColor) {
        super(context, themeColor);
    }

    void switchSettingItemSectionColor(List<TextView> textViewList) {
        Objects.requireNonNull(textViewList);
        textViewList.forEach(Objects::requireNonNull);

        int color = themeColor.getSecondaryContainerColor(resources);
        int onColor = themeColor.getOnSecondaryContainerColor(resources);
        switchTextViewsColor(textViewList, color, onColor);
    }

    void switchSettingItemIconColor(List<TextView> textViewList) {
        Objects.requireNonNull(textViewList);
        textViewList.forEach(Objects::requireNonNull);

        int color = themeColor.getOnSurfaceVariantColor(resources);
        switchTextViewsColorOnlyIcon(textViewList, color);
    }
}
