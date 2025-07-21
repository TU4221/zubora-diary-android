package com.websarva.wings.android.zuboradiary.ui.view.edittext

import android.text.Editable
import android.text.TextWatcher
import android.transition.Transition
import android.widget.EditText
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.websarva.wings.android.zuboradiary.R
import java.util.Arrays

internal class TextInputConfigurator : EditTextConfigurator() {

    private fun TextInputLayout.getEditTextNonNull(): EditText {
        return checkNotNull(editText)
    }

    fun setUpScrollable(vararg textInputLayouts: TextInputLayout) {
        Arrays.stream(textInputLayouts).forEach { x: TextInputLayout ->
            val editText = x.getEditTextNonNull()
            setUpScrollable(editText)
        }
    }

    fun setUpKeyboardCloseOnEnter(vararg textInputLayouts: TextInputLayout) {
        // 入力欄エンターキー押下時の処理。
        Arrays.stream(textInputLayouts).forEach { x: TextInputLayout ->
            val editText = x.getEditTextNonNull()
            setUpKeyboardCloseOnEnter(editText)
        }
    }

    /**
     * クリアボタンを設定するListenerを作成。BaseFragment#addTransitionListenerに渡すこと。<br></br>
     * 上記方法をとる理由は下記不具合が発生する為。<br></br><br></br>
     * xmlファイル、又はレイアウト前の処理コードにてTextInputLayoutのEndIconを設定した時、<br></br>
     * FragmentのTransitionを処理させると、hintラベルがずれる不具合が発生。
     */
    // HACK:xmlファイル、又はレイアウト前のコードにてTextInputLayoutのEndIconを設定した時、FragmentのTransitionを処理させると、
    //      hintラベルがずれる不具合が発生。これはTransitionとラベルのアニメーションが干渉している事が原因らしい。
    //      対策としてTransition完了後にEndIconを設定するようにTransitionListenerを作成し対象Transitionに設定。
    fun createClearButtonSetupTransitionListener(
        vararg textInputLayouts: TextInputLayout): ClearButtonSetUpTransitionListener {

        return ClearButtonSetUpTransitionListener(*textInputLayouts)
    }

    class ClearButtonSetUpTransitionListener(
        private vararg val textInputLayouts: TextInputLayout) : Transition.TransitionListener {

        override fun onTransitionStart(transition: Transition) {
            // 処理なし
        }

        override fun onTransitionEnd(transition: Transition) {
            setUpClearButton(*textInputLayouts)
        }

        override fun onTransitionCancel(transition: Transition) {
            // 処理なし
        }

        override fun onTransitionPause(transition: Transition) {
            // 処理なし
        }

        override fun onTransitionResume(transition: Transition) {
            // 処理なし
        }

        // HACK:クリアボタンはEND_ICON_CLEAR_BUTTONで容易にクリアボタンを実装できるが、
        //      下記の理由でにEND_ICON_CUSTOMを通して設定している。
        //      ・コードからEditTextの文字列を設定してもクリアボタンが表示されない。
        //      ・FragmentのTransition完了後にEndIconを設定しても一度画面からEditTextのTextを編集すると、
        //        フォーカスしない限りクリアボタンが表示されなくなる。
        private fun setUpClearButton(vararg textInputLayouts: TextInputLayout) {
            Arrays.stream(textInputLayouts).forEach { x: TextInputLayout ->
                x.endIconMode = TextInputLayout.END_ICON_CUSTOM
                x.setEndIconDrawable(R.drawable.ic_cancel_24px)

                val textInputEditText = checkNotNull(x.editText) as TextInputEditText

                val text = checkNotNull(textInputEditText.text)
                val isVisible = text.toString().isNotEmpty()
                x.isEndIconVisible = isVisible
                x.setEndIconOnClickListener {
                    textInputEditText.setText("")
                }

                val textWatcher = ClearButtonVisibleSwitchingTextWatcher(x)
                textInputEditText.addTextChangedListener(textWatcher)
            }
        }

        private class ClearButtonVisibleSwitchingTextWatcher(private val textInputLayout: TextInputLayout) :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // 処理なし
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val isVisible = s.toString().isNotEmpty()
                textInputLayout.isEndIconVisible = isVisible
            }

            override fun afterTextChanged(s: Editable) {
                // 処理なし
            }
        }
    }
}
