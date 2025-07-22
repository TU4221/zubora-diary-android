package com.websarva.wings.android.zuboradiary.ui.view.custom

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.view.edittext.EditTextConfigurator

internal class DoneActionTextInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.style.DoneActionTextInputEditTextStyle
) : TextInputEditText(context, attrs, defStyleAttr) {

    init {
        EditTextConfigurator()
            .apply {
                setUpKeyboardCloseOnEnter(this@DoneActionTextInputEditText)
            }
    }
}
