package com.websarva.wings.android.zuboradiary.ui.common.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import com.google.android.material.R
import com.google.android.material.textfield.TextInputEditText
import com.websarva.wings.android.zuboradiary.ui.common.utils.setupScrollable

/**
 * スクロール可能な複数行入力に対応したカスタム[TextInputEditText]。
 *
 * このViewは、[ImeActionHandlingTextInputEditText]を継承し、以下の責務を持つ:
 * - `inputType`を複数行テキスト（`textMultiLine`）に設定する。
 * - `overScrollMode`を設定し、コンテンツがスクロール可能であることを視覚的に示す。
 */
internal class ScrollableMultiLineTextInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.textInputStyle
) : ImeActionHandlingTextInputEditText(context, attrs, defStyleAttr) {

    init {
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        overScrollMode = OVER_SCROLL_IF_CONTENT_SCROLLS
        setupScrollable()
    }
}
