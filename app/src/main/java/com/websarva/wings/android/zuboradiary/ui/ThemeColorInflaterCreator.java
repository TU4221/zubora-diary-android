package com.websarva.wings.android.zuboradiary.ui;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.appcompat.view.ContextThemeWrapper;

import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor;

import java.util.zip.Inflater;

import dagger.internal.Preconditions;

public class ThemeColorInflaterCreator {

    Context context;
    LayoutInflater inflater;
    ThemeColor themeColor;

    public ThemeColorInflaterCreator(Context context, LayoutInflater inflater, ThemeColor themeColor) {
        Preconditions.checkNotNull(inflater);
        Preconditions.checkNotNull(themeColor);

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
