package com.websarva.wings.android.zuboradiary.ui.view

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.databinding.BindingAdapter
import com.google.android.material.appbar.MaterialToolbar

internal object BindingAdapters {
    // MEMO:既存"app:drawableStartCompat"は"@drawable/～"を代入すれば反映されるが、
    //      Drawable型の変数を代入した時はBuildエラーが発生する。これは引数にDrawable型が対応されていない為である。
    //      対策として下記メソッド作成。
    //      (初めは"android:drawableStart"を使用していたが、IDEの警告より"app:drawableStartCompat"に変更。
    //       しかし、現状layoutの構成ではDrawable型の変数を代入したかった為、このような対策をとる。)
    @JvmStatic
    @BindingAdapter("drawableStartCompat")
    fun setDrawableStartCompat(textView: TextView, drawable: Drawable?) {
        if (drawable == null) return
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)
    }

    @JvmStatic
    @BindingAdapter("onNavigationIconClick")
    fun setOnNavigationIconClickListener(toolbar: MaterialToolbar, listener: View.OnClickListener) {
        toolbar.setNavigationOnClickListener(listener)
    }

    @JvmStatic
    @BindingAdapter(
        value = [
            "applySystemInsetsToMarginLeft",
            "applySystemInsetsToMarginTop",
            "applySystemInsetsToMarginRight",
            "applySystemInsetsToMarginBottom"
        ],
        requireAll = false // いずれかの属性が指定されれば呼び出される
    )
    fun applySystemInsetsToMargin(
        view: View,
        applyLeft: Boolean = false,
        applyTop: Boolean = false,
        applyRight: Boolean = false,
        applyBottom: Boolean = false
    ) {
        val initialViewMargin = (view.layoutParams as ViewGroup.MarginLayoutParams)
        val initialViewMarginLeft = initialViewMargin.leftMargin
        val initialViewMarginTop = initialViewMargin.topMargin
        val initialViewMarginRight = initialViewMargin.rightMargin
        val initialViewMarginBottom = initialViewMargin.bottomMargin
        view.apply {
            ViewCompat.setOnApplyWindowInsetsListener(
                this
            ) { view, windowInsets ->
                val insets =
                    windowInsets.getInsets(
                        WindowInsetsCompat.Type.systemBars()
                                or WindowInsetsCompat.Type.displayCutout()
                    )
                view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    if (applyLeft) leftMargin = initialViewMarginLeft + insets.left
                    if (applyTop) topMargin = initialViewMarginTop + insets.top
                    if (applyRight) rightMargin = initialViewMarginRight + insets.right
                    if (applyBottom) bottomMargin = initialViewMarginBottom + insets.bottom
                }
                WindowInsetsCompat.CONSUMED
            }
        }
    }
}
