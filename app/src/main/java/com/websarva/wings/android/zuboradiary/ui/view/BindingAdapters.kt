package com.websarva.wings.android.zuboradiary.ui.view

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
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

    /**
     * 対象Viewの上部に、ステータスバーの高さ分のパディングを適用します。
     * 主にエッジ・トゥ・エッジ表示が有効な場合に使用されます。
     *
     * このアダプターは、Viewに `fitsSystemWindows = true` を設定せずに、
     * ウィンドウインセットのリスナーのみを登録します。リスナー内で、Viewの上部パディングを
     * ステータスバーの高さで更新し、インセットを消費します。
     *
     * 用途：
     * 1. エッジ・トゥ・エッジ環境下で、Toolbar などのビューがステータスバーと重ならないようにする。
     * 2. `fitsSystemWindows = true` を使用した際に発生する、Toolbar の高さが意図せず
     *    変わってしまう問題（特に `?attr/actionBarSize` との組み合わせ時）を回避する代替手段として、
     *    トップパディングのみを精密に制御する。
     *
     *
     * @param view パディングを適用する対象のビュー。
     * @param apply true の場合、ステータスバー上部のパディングを適用します。false の場合は何もしません。
     * */
    @JvmStatic
    @BindingAdapter("applyStatusBarTopPadding")
    fun applyStatusBarTopPadding(view: View, apply: Boolean) {
        if (!apply) return

        view.apply {
            ViewCompat.setOnApplyWindowInsetsListener(
                this
            ) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updatePadding(top = systemBars.top)
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    @JvmStatic
    @BindingAdapter("applyNavigationBarBottomMargin")
    fun applyNavigationBarBottomMargin(view: View, apply: Boolean) {
        if (!apply) return

        val initialViewBottomMargin =
            (view.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
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
                    bottomMargin = initialViewBottomMargin + insets.bottom
                }
                WindowInsetsCompat.CONSUMED
            }
        }
    }
}
