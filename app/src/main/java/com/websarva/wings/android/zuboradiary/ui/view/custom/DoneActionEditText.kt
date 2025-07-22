package com.websarva.wings.android.zuboradiary.ui.view.custom

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.view.edittext.EditTextConfigurator

internal class DoneActionEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = R.style.DoneActionEditTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    init {
        EditTextConfigurator()
            .apply {
                setUpKeyboardCloseOnEnter(this@DoneActionEditText)
            }
    }
}
