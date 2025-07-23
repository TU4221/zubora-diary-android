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
internal class PersistentClearIconTextInputLayout : TextInputLayout {

    // MEMO:デフォルトスタイル属性 (defStyleAttr) を指定せずにインスタンス化する場合のコンストラクタ。
    //      スーパークラスが自身のデフォルトスタイルを適用する。
    @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    init {
        endIconMode = END_ICON_CUSTOM

        // MEMO:"com.google.android.material.R"内は変更、削除される可能性がある為、参照すを控えるように警告が表示される。
        //      その為、アプリで用意したキャンセルアイコンをセットする。
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
