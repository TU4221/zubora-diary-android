package com.websarva.wings.android.zuboradiary.ui.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.websarva.wings.android.zuboradiary.ui.keyboard.KeyboardManager

/**
 * EditTextなどからフォーカスを外し、キーボードを非表示にするための背景View。
 *
 * このViewをクリックすると、現在フォーカスを持っているUIコンポーネントからフォーカスをクリアし、
 * 表示されているソフトウェアキーボードを隠す責務を持つ。
 * 主に、入力フォームの後ろに配置して使用する。
 */
internal class ClearFocusBackgroundView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    /** ソフトウェアキーボードを制御するマネージャークラス。 */
    private val keyboardManager = KeyboardManager(context)

    init {
        setupFocusClearOnClick()
    }

    /**
     * このViewがクリックされたときにフォーカスをクリアし、
     * キーボードを非表示にするためのリスナーを設定する。
     */
    private fun setupFocusClearOnClick() {
        // MEMO:BackgroundViewのクリック処理で自身にフォーカスを当てるには、isFocusableInTouchModeを有効にした状態で
        //      requestFocus()を呼び出す必要がある。isFocusableInTouchModeを有効にしたままBackGroundViewをタッチすると、
        //      クリック処理は行われず、フォーカスのみがあてられる。そのためキーボードが非表示にならず、
        //      BackGroundViewにあわせたキーボードに切り替わる。フォーカスされた状態で再度タッチするとクリック処理が行われる。
        //      上記を踏まえて下記コードで対応。
        isClickable = true
        isFocusable = true
        setOnClickListener { v: View ->
            keyboardManager.hideKeyboard(v)

            isFocusableInTouchMode = true
            v.requestFocus()
            v.clearFocus()
            isFocusableInTouchMode = false
        }
    }
}
