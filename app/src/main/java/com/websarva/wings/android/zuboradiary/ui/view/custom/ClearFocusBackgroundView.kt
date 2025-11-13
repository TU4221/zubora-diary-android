package com.websarva.wings.android.zuboradiary.ui.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.websarva.wings.android.zuboradiary.ui.keyboard.KeyboardManager

internal class ClearFocusBackgroundView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val keyboardManager = KeyboardManager(context)

    init {
        setUpFocusClearOnClick()
    }

    private fun setUpFocusClearOnClick() {
        // MEMO:BackGroundViewのクリック処理で自身にフォーカスを当てるには、isFocusableInTouchModeを有効にした状態で
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
