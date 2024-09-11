package com.websarva.wings.android.zuboradiary.ui.settings;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;
import com.websarva.wings.android.zuboradiary.ui.BaseThemeColorSwitcher;
import com.websarva.wings.android.zuboradiary.ui.ColorSwitchingViewList;

class SettingsThemeColorSwitcher extends BaseThemeColorSwitcher {
    SettingsThemeColorSwitcher(Context context, ThemeColor themeColor) {
        super(context, themeColor);
    }

    void switchSettingItemSectionColor(ColorSwitchingViewList<TextView> textViewList) {
        if (textViewList == null) {
            throw new NullPointerException();
        }

        int surfaceContainerColor = themeColor.getSurfaceContainerColor(resources);
        int onSurfaceColor = themeColor.getOnSurfaceColor(resources);
        switchTextViewColor(surfaceContainerColor, onSurfaceColor, textViewList);
    }

    void switchSettingItemIconColor(ColorSwitchingViewList<TextView> textViewList) {
        if (textViewList == null) {
            throw new NullPointerException();
        }

        int primaryColor = themeColor.getPrimaryColor(resources);
        switchTextViewColorOnlyIcon(primaryColor, textViewList);
    }
}
