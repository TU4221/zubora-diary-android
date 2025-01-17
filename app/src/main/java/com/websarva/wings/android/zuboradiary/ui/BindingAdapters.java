package com.websarva.wings.android.zuboradiary.ui;

import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

public class BindingAdapters {

    // MEMO:既存"app:drawableStartCompat"は"@drawable/～"を代入すれば反映されるが、
    //      Drawable型の変数を代入した時はBuildエラーが発生する。これは引数にDrawable型が対応されていない為である。
    //      対策として下記メソッド作成。
    //      (初めは"android:drawableStart"を使用していたが、IDEの警告より"app:drawableStartCompat"に変更。
    //       しかし、現状layoutの構成ではDrawable型の変数を代入したかった為、このような対策をとる。)
    @BindingAdapter("drawableStartCompat")
    public static void setDrawableStartCompat(TextView textView, Drawable drawable) {
        if (drawable == null) return;
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null);
    }
}
