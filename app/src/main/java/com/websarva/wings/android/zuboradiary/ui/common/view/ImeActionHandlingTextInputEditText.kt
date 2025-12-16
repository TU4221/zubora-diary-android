package com.websarva.wings.android.zuboradiary.ui.common.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.R
import com.google.android.material.textfield.TextInputEditText
import com.websarva.wings.android.zuboradiary.ui.common.utils.setupKeyboardCloseOnEnter

/**
 * IMEアクション（「完了」や「次へ」など）が実行された際に、ソフトウェアキーボードを閉じるなどの
 * 共通的な動作を自動的に設定するカスタム[TextInputEditText]。
 */
internal open class ImeActionHandlingTextInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) : TextInputEditText(context, attrs, defStyleAttr) {

    init {
        setupKeyboardCloseOnEnter()
    }
}
