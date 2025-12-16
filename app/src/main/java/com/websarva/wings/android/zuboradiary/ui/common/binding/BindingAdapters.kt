package com.websarva.wings.android.zuboradiary.ui.common.binding

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
import com.google.android.material.textfield.TextInputLayout
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.common.state.InputTextValidationState
import com.websarva.wings.android.zuboradiary.ui.common.utils.formatDateString
import com.websarva.wings.android.zuboradiary.ui.common.view.ImageProgressView
import java.time.LocalDate

/**
 * アプリケーション全体で使用されるカスタムBinding Adapterを定義するオブジェクト。
 */
internal object BindingAdapters {
    /**
     * [android.widget.TextView]に[android.graphics.drawable.Drawable]を開始アイコンとして設定する。
     *
     * 標準の`app:drawableStartCompat`はXMLリソース（`@drawable/...`）の直接指定はサポートするが、
     * DataBindingからの[android.graphics.drawable.Drawable]オブジェクトは対応していない為、このカスタムアダプターを使用する。
     *
     * @param textView 対象のTextView。
     * @param drawable 設定するDrawable。nullの場合は何もしない。
     */
    @JvmStatic
    @BindingAdapter("drawableStartCompat")
    fun setDrawableStartCompat(textView: TextView, drawable: Drawable?) {
        if (drawable == null) return
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)
    }

    /**
     * [com.google.android.material.appbar.MaterialToolbar]のナビゲーションアイコンにクリックリスナーを設定する。
     * @param toolbar 対象のToolbar。
     * @param listener 設定するクリックリスナー。
     */
    @JvmStatic
    @BindingAdapter("onNavigationIconClick")
    fun setOnNavigationIconClickListener(toolbar: MaterialToolbar, listener: View.OnClickListener) {
        toolbar.setNavigationOnClickListener(listener)
    }

    /**
     * [AutoCompleteTextView](ドロップダウンリストのアイテム等)にクリックリスナーを設定する。
     * @param view 対象のAutoCompleteTextView。
     * @param listener 設定するアイテムクリックリスナー。
     */
    @JvmStatic
    @BindingAdapter("onItemClick")
    fun setOnItemClick(
        view: AutoCompleteTextView,
        listener: AdapterView.OnItemClickListener?) {
        view.onItemClickListener = listener
    }

    /**
     * [com.google.android.material.textfield.TextInputLayout]に、指定された[com.websarva.wings.android.zuboradiary.ui.common.state.InputTextValidationState]に基づいたエラーメッセージを設定する。
     * @param layout 対象のTextInputLayout。
     * @param state バリデーションの状態。`Valid`または`null`の場合はエラーをクリアする。
     */
    @JvmStatic
    @BindingAdapter("textValidationState")
    fun setTextValidation(
        layout: TextInputLayout,
        state: InputTextValidationState?
    ) {
        val context = layout.context
        layout.error =
            when (state) {
                InputTextValidationState.Valid -> null
                InputTextValidationState.Invalid -> ""
                InputTextValidationState.InvalidEmpty -> {
                    context.getString(R.string.dialog_diary_item_title_edit_new_item_title_input_field_error_message_empty)
                }
                InputTextValidationState.InvalidInitialCharUnmatched -> {
                    context.getString(R.string.dialog_diary_item_title_edit_new_item_title_input_field_error_message_initial_char_unmatched)
                }
                null -> null
            }
    }

    /**
     * [ImageProgressView]に、指定されたファイルパスから画像を読み込む。
     * @param imageView 対象のImageProgressView。
     * @param filePath 読み込む画像のファイルパス。
     */
    @JvmStatic
    @BindingAdapter("imagePath")
    fun setImageProgressViewLoadImagePath(imageView: ImageProgressView, filePath: String?) {
        imageView.loadImage(filePath)
    }

    /**
     * [java.time.LocalDate]を日本語の日付書式にフォーマットし、[TextView]に設定する。
     * @param textView 対象のTextView。
     * @param date フォーマットする日付。
     */
    @JvmStatic
    @BindingAdapter("dateText")
    fun setDateText(textView: TextView, date: LocalDate) {
        val dateText = date.formatDateString(textView.context)
        if (textView.text.toString() != dateText) {
            textView.text = dateText
        }
    }

    /**
     * [LocalDate]を日本語の日付書式にフォーマットし、[androidx.appcompat.widget.Toolbar]のタイトルとして設定する。
     * @param toolbar 対象のToolbar。
     * @param selectedCalendarDate フォーマットする日付。
     */
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
     *  対応するViewHolder([com.websarva.wings.android.zuboradiary.ui.common.view.WindowInsetsViewHolder]) を継承する必要あり。
     *  これにより、ViewHolderのItemViewがウィンドウにアタッチされた際にインセットの適用が要求され、
     *  初期表示時にインセットが反映。
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
        with(view) {
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
     *  対応するViewHolder([com.websarva.wings.android.zuboradiary.ui.common.view.WindowInsetsViewHolder]) を継承する必要あり。
     *  これにより、ViewHolderのItemViewがウィンドウにアタッチされた際にインセットの適用が要求され、
     *  初期表示時にインセットが反映。
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
        with(view) {
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
