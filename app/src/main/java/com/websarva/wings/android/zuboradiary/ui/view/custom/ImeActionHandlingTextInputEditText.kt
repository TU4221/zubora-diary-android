package com.websarva.wings.android.zuboradiary.ui.view.custom

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText
import com.websarva.wings.android.zuboradiary.ui.view.edittext.EditTextConfigurator

/**
 * IMEアクション（「完了」や「次へ」など）が実行された際に、ソフトウェアキーボードを閉じるなどの
 * 共通的な動作を自動的に設定するカスタム[TextInputEditText]。
 *
 * このViewは、内部で[EditTextConfigurator]を利用して、キーボード制御をセットアップする。
 */
internal open class ImeActionHandlingTextInputEditText : TextInputEditText {

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
        EditTextConfigurator().setupKeyboardCloseOnEnter(this)
    }
}
