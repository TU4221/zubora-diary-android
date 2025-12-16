package com.websarva.wings.android.zuboradiary.ui.common.view

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputLayout
import com.websarva.wings.android.zuboradiary.R

/**
 * テキストが空でない場合にのみ、末尾にクリアアイコン（×ボタン）を常に表示するカスタム[TextInputLayout]。
 *
 * 標準の`endIconMode = END_ICON_CLEAR_TEXT`は、EditTextがフォーカスを持っているときにのみクリアアイコンを表示するが、
 * このカスタムViewはフォーカスの有無にかかわらず、テキストが存在すれば常にアイコンを表示する責務を持つ。
 */
internal class PersistentClearIconTextInputLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.textInputStyle
) : TextInputLayout(context, attrs, defStyleAttr) {

    init {
        setupPersistentClearIcon()
    }

    /**
     * クリアアイコンの表示モード、Drawable、および表示/非表示とクリックのロジックを設定する。
     */
    private fun setupPersistentClearIcon() {
        endIconMode = END_ICON_CUSTOM

        // MEMO:"com.google.android.material.R"内は変更、削除される可能性がある為、参照すを控えるように警告が表示される。
        //      その為、アプリで用意したキャンセルアイコンをセットする。
        setEndIconDrawable(R.drawable.ic_cancel_24px)

        addOnEditTextAttachedListener {
            val editText = checkNotNull(it.editText)
            isEndIconVisible = editText.text.toString().isNotEmpty()

            // アイコンクリックでテキストを空にする
            setEndIconOnClickListener {
                editText.setText("")
            }

            // テキストの変更を監視してアイコンの表示/非表示を切り替え
            editText.addTextChangedListener(
                onTextChanged = { text, _, _, _ ->
                    isEndIconVisible = text.toString().isNotEmpty()
                }
            )
        }
    }
}
