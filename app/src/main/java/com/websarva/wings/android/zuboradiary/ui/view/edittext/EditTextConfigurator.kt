package com.websarva.wings.android.zuboradiary.ui.view.edittext

import android.text.InputType
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.View.OnTouchListener
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import com.websarva.wings.android.zuboradiary.ui.keyboard.KeyboardManager

internal class EditTextConfigurator {

    fun setUpScrollable(editText: EditText) {
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

    fun setUpKeyboardCloseOnEnter(editText: EditText) {
        editText.setOnEditorActionListener(NextOnEnterListener())
    }

    private class NextOnEnterListener : TextView.OnEditorActionListener {

        private lateinit var keyboardManager : KeyboardManager

        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
            v as EditText

            // android:inputType="textMultiLine"
            if (
                v.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                ) return false

            if (!::keyboardManager.isInitialized) {
                keyboardManager = KeyboardManager(v.context)
            }
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    keyboardManager.hideKeyboard(v)
                    v.clearFocus()
                }
                EditorInfo.IME_ACTION_NEXT -> {
                    val nextView = focusNextEditTextView(v)

                    if (nextView?.inputType == InputType.TYPE_NULL) keyboardManager.hideKeyboard(v)
                }
                EditorInfo.IME_ACTION_SEARCH -> {
                    keyboardManager.hideKeyboard(v)
                    v.clearFocus()
                }
                else -> keyboardManager.hideKeyboard(v)
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

            if (nextView == null) return null
            nextView.requestFocus()
            return nextView
        }
    }
}
