package com.websarva.wings.android.zuboradiary.ui;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.appcompat.view.ContextThemeWrapper;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;

import java.util.Objects;

public class ThemeColorInflaterCreator {

    private final Context context;
    private final LayoutInflater inflater;
    private final ThemeColor themeColor;

    public ThemeColorInflaterCreator(Context context, LayoutInflater inflater, ThemeColor themeColor) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(inflater);
        Objects.requireNonNull(themeColor);

        this.context = context;
        this.inflater = inflater;
        this.themeColor = themeColor;
    }

    public LayoutInflater create(){
        int themeResId = themeColor.getThemeResId();
        Context contextWithTheme = new ContextThemeWrapper(context, themeResId);
        return inflater.cloneInContext(contextWithTheme);
    }
}
