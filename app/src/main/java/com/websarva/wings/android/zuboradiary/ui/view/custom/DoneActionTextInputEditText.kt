package com.websarva.wings.android.zuboradiary.ui.view.custom

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import com.google.android.material.textfield.TextInputEditText
import com.websarva.wings.android.zuboradiary.ui.view.edittext.EditTextConfigurator

internal class DoneActionTextInputEditText : TextInputEditText {

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
        imeOptions = EditorInfo.IME_ACTION_DONE
        inputType = InputType.TYPE_CLASS_TEXT
        EditTextConfigurator()
            .apply {
                setUpKeyboardCloseOnEnter(this@DoneActionTextInputEditText)
            }
    }
}
