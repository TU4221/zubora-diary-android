package com.websarva.wings.android.zuboradiary.ui.view

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.databinding.BindingAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.websarva.wings.android.zuboradiary.ui.utils.formatDateString
import com.websarva.wings.android.zuboradiary.ui.view.custom.ImageProgressView
import com.websarva.wings.android.zuboradiary.ui.view.custom.WindowInsetsViewHolder
import java.time.LocalDate

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
    @BindingAdapter("onItemClick")
    fun setOnItemClick(
        view: AutoCompleteTextView,
        listener: AdapterView.OnItemClickListener?) {
        view.onItemClickListener = listener
    }

    /**
     * ImageProgressView にカスタム属性を介してクリックリスナーを設定するためのBindingAdapter。
     */
    @JvmStatic
    @BindingAdapter("onImageClick")
    fun setImageProgressViewClickListener(view: ImageProgressView, listener: View.OnClickListener?) {
        view.setImageOnClickListener(listener)
    }

    @JvmStatic
    @BindingAdapter("imagePath")
    fun setImageProgressViewLoadImagePath(imageView: ImageProgressView, filePath: String?) {
        imageView.loadImage(filePath)
    }

    @JvmStatic
    @BindingAdapter("dateText")
    fun setDateText(textView: TextView, date: LocalDate) {
        val dateText = date.formatDateString(textView.context)
        if (textView.text.toString() != dateText) {
            textView.text = dateText
        }
    }

    @JvmStatic
    @BindingAdapter("dateTitle")
    fun setDateTitle(toolbar: Toolbar, selectedCalendarDate: LocalDate) {
        val dateText = selectedCalendarDate.formatDateString(toolbar.context)
        toolbar.title?.let {
            if (it.toString() == dateText) return
        }

        toolbar.title = dateText
    }


    /**
     * 指定されたViewのマージンにシステムバー（ステータスバー、ナビゲーションバーなど）や
     * ディスプレイカットアウトのインセット値を加算。
     *
     * このBindingAdapterは、ViewがシステムUI要素によって隠れないようにするために使用。(Edge-to-Edge対応)
     *
     * **RecyclerViewのアイテムビューで使用する場合の重要な注意点:**
     *  このBindingAdapterがアイテムの初期表示時にインセットを確実に反映するためには、
     *  対応するViewHolder([WindowInsetsViewHolder]) を継承する必要あり。
     *  これにより、ViewHolderのItemViewがウィンドウにアタッチされた際にインセットの適用が要求され、初期表示時にインセットが反映。
     *
     * @param view マージンを調整するView。
     * @param applyLeft trueの場合、左マージンにインセットを適用します。
     * @param applyTop trueの場合、上マージンにインセットを適用します。
     * @param applyRight trueの場合、右マージンにインセットを適用します。
     * @param applyBottom trueの場合、下マージンにインセットを適用します。
     */
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
                windowInsets
            }
        }
    }

    /**
     * 指定されたViewのパディングにシステムバー（ステータスバー、ナビゲーションバーなど）や
     * ディスプレイカットアウトのインセット値を加算。
     *
     * このBindingAdapterは、ViewがシステムUI要素によって隠れないようにするために使用。(Edge-to-Edge対応)
     *
     * **RecyclerViewのアイテムビューで使用する場合の重要な注意点:**
     *  このBindingAdapterがアイテムの初期表示時にインセットを確実に反映するためには、
     *  対応するViewHolder([WindowInsetsViewHolder]) を継承する必要あり。
     *  これにより、ViewHolderのItemViewがウィンドウにアタッチされた際にインセットの適用が要求され、初期表示時にインセットが反映。
     *
     * @param view パディングを調整するView。
     * @param applyLeft trueの場合、左パディングにインセットを適用します。
     * @param applyTop trueの場合、上パディングにインセットを適用します。
     * @param applyRight trueの場合、右パディングにインセットを適用します。
     * @param applyBottom trueの場合、下パディングにインセットを適用します。
     */
    @JvmStatic
    @BindingAdapter(
        value = [
            "applySystemInsetsToPaddingLeft",
            "applySystemInsetsToPaddingTop",
            "applySystemInsetsToPaddingRight",
            "applySystemInsetsToPaddingBottom"
        ],
        requireAll = false // いずれかの属性が指定されれば呼び出される
    )
    fun applySystemInsetsToPadding(
        view: View,
        applyLeft: Boolean = false,
        applyTop: Boolean = false,
        applyRight: Boolean = false,
        applyBottom: Boolean = false
    ) {
        val initialViewPaddingLeft = view.paddingLeft
        val initialViewPaddingTop = view.paddingTop
        val initialViewPaddingRight = view.paddingRight
        val initialViewPaddingBottom = view.paddingBottom
        view.apply {
            ViewCompat.setOnApplyWindowInsetsListener(
                this
            ) { view, windowInsets ->
                val insets =
                    windowInsets.getInsets(
                        WindowInsetsCompat.Type.systemBars()
                                or WindowInsetsCompat.Type.displayCutout()
                    )
                view.updatePadding(
                    left = if (applyLeft) initialViewPaddingLeft + insets.left else view.paddingLeft,
                    top = if (applyTop) initialViewPaddingTop + insets.top else view.paddingTop,
                    right = if (applyRight) initialViewPaddingRight + insets.right else view.paddingRight,
                    bottom = if (applyBottom) initialViewPaddingBottom + insets.bottom else view.paddingBottom
                )
                windowInsets
            }
        }
    }
}
