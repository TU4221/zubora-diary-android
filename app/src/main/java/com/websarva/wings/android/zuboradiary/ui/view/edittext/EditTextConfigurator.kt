package com.websarva.wings.android.zuboradiary.ui.view.edittext

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.View.OnTouchListener
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.websarva.wings.android.zuboradiary.ui.keyboard.KeyboardManager
import java.util.Arrays

internal open class EditTextConfigurator {

    protected fun hideKeyboard(view: View) {
        KeyboardManager().hideKeyboard(view)
    }

    protected fun setUpScrollable(editText: EditText) {
        editText.onFocusChangeListener = EditTextScrollableOnFocusChangeListener()
    }

    private class EditTextScrollableOnFocusChangeListener : OnFocusChangeListener {
        override fun onFocusChange(v: View, hasFocus: Boolean) {
            if (hasFocus) {
                setUpEditTextScrollable(v)
            } else {
                resetEditTextScrollable(v)
            }
        }

        fun setUpEditTextScrollable(focusedView: View) {
            val editText = focusedView as EditText
            if (editText.minLines > 1) focusedView.setOnTouchListener(ScrollableTextOnTouchListener())
        }

        fun resetEditTextScrollable(focusedView: View) {
            val editText = focusedView as EditText
            if (editText.minLines > 1) focusedView.setOnTouchListener(null)
        }

        private class ScrollableTextOnTouchListener : OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (!v.canScrollVertically(1) && !v.canScrollVertically(-1)) return false

                v.parent.requestDisallowInterceptTouchEvent(true)
                if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    v.performClick()
                }
                return false
            }
        }
    }

    fun setUpKeyboardCloseOnEnter(vararg editTexts: EditText) {
        Arrays.stream(editTexts)
            .forEach { editText: EditText -> this.setUpKeyboardCloseOnEnter(editText) }
    }

    protected fun setUpKeyboardCloseOnEnter(editText: EditText) {
        editText.setOnEditorActionListener(NextOnEnterListener())
    }

    private inner class NextOnEnterListener : TextView.OnEditorActionListener {

        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
            v as EditText

            // HACK:InputTypeの値が何故か1ズレている。(公式のリファレンスでもズレあり。)(setとgetを駆使してLogで確認確認済み)
            if (v.inputType == (InputType.TYPE_TEXT_FLAG_MULTI_LINE + 1)) return false

            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    hideKeyboard(v)
                    v.clearFocus()
                }
                EditorInfo.IME_ACTION_NEXT -> {
                    val nextView = focusNextEditTextView(v)

                    if (nextView?.inputType == InputType.TYPE_NULL) hideKeyboard(v)
                }
                EditorInfo.IME_ACTION_SEARCH -> {
                    hideKeyboard(v)
                    v.clearFocus()
                }
                else -> hideKeyboard(v)
            }
            return false // MEMO:”return true” だとバックスペースが機能しなくなり入力文字を削除できなくなる。
        }

        private fun focusNextEditTextView(view: View): EditText? {
            var offsetView = view
            var nextView: View?
            do {
                nextView = offsetView.focusSearch(View.FOCUS_DOWN)
                offsetView = nextView
            } while (nextView !is EditText && nextView != null)

            if (nextView == null || nextView !is EditText) return null
            nextView.requestFocus()
            return nextView
        }
    }

    fun setUpFocusClearOnClickBackground(background: View) {
        // MEMO:BackGroundViewのクリック処理で自身にフォーカスを当てるには、isFocusableInTouchModeを有効にした状態で
        //      requestFocus()を呼び出す必要がある。isFocusableInTouchModeを有効にしたままBackGroundViewをタッチすると、
        //      クリック処理は行われず、フォーカスのみがあてられる。そのためキーボードが非表示にならず、
        //      BackGroundViewにあわせたキーボードに切り替わる。フォーカスされた状態で再度タッチするとクリック処理が行われる。
        //      上記を踏まえて下記コードで対応。
        background.apply {
            isClickable = true
            isFocusable = true
            setOnClickListener { v: View ->
                hideKeyboard(v)

                isFocusableInTouchMode = true
                v.requestFocus()
                v.clearFocus()
                isFocusableInTouchMode = false
            }
        }
    }

    fun setUpClearButton(editText: EditText, clearButton: ImageButton) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // 処理なし
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val isVisible = s.toString().isNotEmpty()
                val visibility = if (isVisible) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
                clearButton.visibility = visibility
            }

            override fun afterTextChanged(s: Editable) {
                // 処理なし
            }
        })

        clearButton.visibility = View.INVISIBLE
        clearButton.setOnClickListener { editText.setText("") }
    }
}
