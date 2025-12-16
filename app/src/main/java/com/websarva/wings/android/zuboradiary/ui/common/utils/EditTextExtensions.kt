package com.websarva.wings.android.zuboradiary.ui.common.utils

import android.annotation.SuppressLint
import android.text.InputType
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import com.websarva.wings.android.zuboradiary.ui.common.keyboard.KeyboardManager

/**
 * 複数行の[EditText]が、親のスクロール可能コンテナ内で自身のコンテンツをスクロールできるように設定する。
 *
 * 親Layout（ScrollViewなど）のタッチイベントインターセプトを制御し、
 * EditText内部のスクロール操作を優先させる。
 */
fun EditText.setupScrollable() {
    this.onFocusChangeListener = EditTextScrollableOnFocusChangeListener()
}

/**
 * [EditText]でIMEアクション（「完了」、「次へ」など）が実行された際に、キーボードを閉じるなどの動作を設定する。
 *
 * アクションIDに応じて、キーボードの非表示や次の[EditText]へのフォーカス移動を行う。
 */
fun EditText.setupKeyboardCloseOnEnter() {
    this.setOnEditorActionListener(NextOnEnterListener())
}

/**
 * [EditText]がフォーカスを得た時にスクロール用のタッチリスナーを設定し、フォーカスを失った時に解除するリスナー。
 */
private class EditTextScrollableOnFocusChangeListener : View.OnFocusChangeListener {
    /** EditTextのフォーカス状態が変更されたときに呼び出される。 */
    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            setupEditTextScrollable(v)
        } else {
            resetEditTextScrollable(v)
        }
    }

    /**
     * 複数行[EditText]にスクロール用のタッチリスナーを設定する。
     * @param focusedView フォーカスを得たView。
     */
    private fun setupEditTextScrollable(focusedView: View) {
        val editText = focusedView as EditText
        if (editText.minLines > 1) focusedView.setOnTouchListener(ScrollableTextOnTouchListener())
    }

    /**
     * [EditText]からスクロール用のタッチリスナーを解除する。
     * @param focusedView フォーカスを失ったView。
     */
    private fun resetEditTextScrollable(focusedView: View) {
        val editText = focusedView as EditText
        if (editText.minLines > 1) focusedView.setOnTouchListener(null)
    }
}

/**
 * [EditText]が垂直方向にスクロール可能な場合、親Viewにタッチイベントのインターセプトを要求しないようにし、
 * [EditText]自体のスクロールを優先させるタッチリスナー。
 */
private class ScrollableTextOnTouchListener : View.OnTouchListener {
    /** EditTextに対するタッチイベントを処理する。 */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (!v.canScrollVertically(1) && !v.canScrollVertically(-1)) {
            return false
        }

        v.parent.requestDisallowInterceptTouchEvent(true)
        if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            v.parent.requestDisallowInterceptTouchEvent(false)
            v.performClick()
        }
        return false
    }
}

/**
 * アクションIDに応じて、キーボードの非表示や次の[EditText]へのフォーカス移動を行うリスナー。
 */
private class NextOnEnterListener : TextView.OnEditorActionListener {

    private lateinit var keyboardManager: KeyboardManager

    /** IMEアクションが実行されたときに呼び出される。 */
    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        // v は null になることは稀だが、安全のためスマートキャストまたは早期リターン
        val editText = v as? EditText ?: return false

        // android:inputType="textMultiLine" の場合はEnterキーでの改行を優先するため何もしない
        if (editText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE)) {
            return false
        }

        if (!::keyboardManager.isInitialized) {
            keyboardManager = KeyboardManager(editText.context)
        }

        when (actionId) {
            EditorInfo.IME_ACTION_DONE -> {
                keyboardManager.hideKeyboard(editText)
                editText.clearFocus()
            }
            EditorInfo.IME_ACTION_NEXT -> {
                val nextView = focusNextEditTextView(editText)
                // 次のViewが入力不可(TYPE_NULL)ならキーボードを閉じる
                if (nextView?.inputType == InputType.TYPE_NULL) {
                    keyboardManager.hideKeyboard(editText)
                }
            }
            EditorInfo.IME_ACTION_SEARCH -> {
                keyboardManager.hideKeyboard(editText)
                editText.clearFocus()
            }
            else -> {
                keyboardManager.hideKeyboard(editText)
            }
        }
        // MEMO: "return true" だとバックスペースが機能しなくなり入力文字を削除できなくなることがあるため false を返す
        return false
    }

    /**
     * 指定されたViewの次にあるEditTextを見つけてフォーカスを当てる。
     * @param view 現在フォーカスを持っているView。
     * @return 次のEditText。見つからない場合は`null`。
     */
    private fun focusNextEditTextView(view: View): EditText? {
        var offsetView = view
        var nextView: View?
        do {
            nextView = offsetView.focusSearch(View.FOCUS_DOWN)
            offsetView = nextView ?: break
        } while (nextView !is EditText)

        if (nextView == null) return null
        nextView.requestFocus()
        return nextView as EditText
    }
}
