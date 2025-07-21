package com.websarva.wings.android.zuboradiary.ui.view.custom

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputLayout
import com.websarva.wings.android.zuboradiary.R

/**
 * 標準の `TextInputLayout.END_ICON_CLEAR_TEXT` モードでは、クリアアイコンは
 * EditText がフォーカスを持っており、かつテキストが入力されている場合にのみ表示する。
 * このカスタム TextInputLayout は、EditText のフォーカス状態に関わらず、
 * テキストが存在する限り常にカスタムのクリアアイコンを表示し続けるようにする。
 */
internal class PersistentClearIconTextInputLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextInputLayout(context, attrs, defStyleAttr) {
    init {
        endIconMode = END_ICON_CUSTOM

        // TODO:"com.google.android.material.R"から参照したいが、AndroidStudioの不具合で参照できないため、
        //      仮でアプリのRクラスから参照。
        setEndIconDrawable(R.drawable.ic_cancel_24px)

        addOnEditTextAttachedListener {
            val editText = checkNotNull(it.editText)
            isEndIconVisible = editText.text.toString().isNotEmpty()
            setEndIconOnClickListener {
                editText.setText("")
            }
            editText.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    isEndIconVisible = text.toString().isNotEmpty()
                }
            )
        }
    }
}
