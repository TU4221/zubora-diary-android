package com.websarva.wings.android.zuboradiary.ui.utils

import android.app.Activity
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.View.OnTouchListener
import android.widget.EditText
import android.widget.ImageButton
import java.util.Arrays

internal open class EditTextSetup(private val activity: Activity) {

    protected fun hideKeyboard(view: View) {
        val keyboardInitializer = KeyboardInitializer(activity)
        keyboardInitializer.hide(view)
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
        editText.setOnKeyListener(KeyboardCloseOnEnterListener())
    }

    private inner class KeyboardCloseOnEnterListener : View.OnKeyListener {
        override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
            if (event.action != KeyEvent.ACTION_DOWN) return false
            if (keyCode != KeyEvent.KEYCODE_ENTER) return false

            val editText = v as EditText
            // HACK:InputTypeの値が何故か1ズレている。(公式のリファレンスでもズレあり。)(setとgetを駆使してLogで確認確認済み)
            if (editText.inputType == (InputType.TYPE_TEXT_FLAG_MULTI_LINE + 1)) return false

            hideKeyboard(v)
            editText.clearFocus()

            return false // MEMO:”return true” だとバックスペースが機能しなくなり入力文字を削除できなくなる。
        }
    }

    fun setUpFocusClearOnClickBackground(background: View, vararg editTexts: EditText) {
        background.setOnClickListener { v: View ->
            hideKeyboard(v)
            Arrays.stream(editTexts).forEach { obj: EditText -> obj.clearFocus() }
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
