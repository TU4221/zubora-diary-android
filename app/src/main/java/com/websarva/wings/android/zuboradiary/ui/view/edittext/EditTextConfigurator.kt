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

/**
 * EditTextの共通的な動作を設定するためのコンフィギュレータクラス。
 *
 * このクラスは、以下のようなEditTextの挙動をカプセル化し、再利用可能な形で提供する:
 * - 親がスクロール可能なレイアウト内での、複数行EditTextのスクロール対応。
 * - IMEアクション（「完了」、「次へ」など）に応じたキーボードの表示/非表示やフォーカス移動の制御。
 */
internal class EditTextConfigurator {

    /**
     * 複数行の[EditText]が、親のスクロール可能コンテナ内で自身のコンテンツをスクロールできるように設定する。
     * @param editText セットアップ対象のEditText。
     */
    fun setupScrollable(editText: EditText) {
        editText.onFocusChangeListener = EditTextScrollableOnFocusChangeListener()
    }

    /**
     * [EditText]がフォーカスを得た時にスクロール用のタッチリスナーを設定し、フォーカスを失った時に解除する。
     */
    private class EditTextScrollableOnFocusChangeListener : OnFocusChangeListener {
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
        fun setupEditTextScrollable(focusedView: View) {
            val editText = focusedView as EditText
            if (editText.minLines > 1) focusedView.setOnTouchListener(ScrollableTextOnTouchListener())
        }

        /**
         * [EditText]からスクロール用のタッチリスナーを解除する。
         * @param focusedView フォーカスを失ったView。
         */
        fun resetEditTextScrollable(focusedView: View) {
            val editText = focusedView as EditText
            if (editText.minLines > 1) focusedView.setOnTouchListener(null)
        }

        /**
         * [EditText]が垂直方向にスクロール可能な場合、親Viewにタッチイベントのインターセプトを要求しないようにし、
         * [EditText]自体のスクロールを優先させる。
         */
        private class ScrollableTextOnTouchListener : OnTouchListener {
            /** EditTextに対するタッチイベントを処理する。 */
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (
                    !v.canScrollVertically(1)
                        && !v.canScrollVertically(-1)
                    ) return false

                v.parent.requestDisallowInterceptTouchEvent(true)
                if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    v.performClick()
                }
                return false
            }
        }
    }

    /**
     * [EditText]でIMEアクション（「完了」、「次へ」など）が実行された際に、キーボードを閉じるなどの動作を設定する。
     * @param editText セットアップ対象のEditText。
     */
    fun setupKeyboardCloseOnEnter(editText: EditText) {
        editText.setOnEditorActionListener(NextOnEnterListener())
    }

    /**
     * アクションIDに応じて、キーボードの非表示や次の[EditText]へのフォーカス移動を行う。
     */
    private class NextOnEnterListener : TextView.OnEditorActionListener {

        private lateinit var keyboardManager : KeyboardManager

        /** IMEアクションが実行されたときに呼び出される。 */
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
                offsetView = nextView
            } while (nextView !is EditText && nextView != null)

            if (nextView == null) return null
            nextView.requestFocus()
            return nextView
        }
    }
}
