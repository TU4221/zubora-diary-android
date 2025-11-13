package com.websarva.wings.android.zuboradiary.ui.view.custom

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import com.websarva.wings.android.zuboradiary.ui.view.edittext.EditTextConfigurator

internal class ScrollableMultiLineTextInputEditText : ImeActionHandlingTextInputEditText {

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
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        overScrollMode = OVER_SCROLL_IF_CONTENT_SCROLLS
        EditTextConfigurator().setUpScrollable(this)
    }
}
