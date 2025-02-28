package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.textfield.TextInputEditText

/**
 * 本クラスは”TextInputEditText”をタッチした時、一度のタッチで"OnClickListener"が処理されるようにカスタマイズしたクラスである。
 * ”TextInputLayout”に"OnClickListener"を設定しても処理はされない。
 */
// MEMO:"TextInputEditText"にOnTouchListenerをセットすると
//      警告"Custom view has setOnTouchListener called on it but does not override performClick"が表示される。
//      これは本来のアクセシビリティサービスが損なわれる可能性があることを示唆している。
//      対策としてonTouchEvent()メソッドをOverrideした本クラスを作成して使用。
//      https://www.binarydevelop.com/article/androidsetontouchlistenerperformclick-68009
class CustomTextInputEditText : TextInputEditText {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) performClick()
        return super.onTouchEvent(event)
    }

    // MEMO:警告"Custom view overrides `onTouchEvent` but not `performClick`"対策として下記メソッド用意
    @Suppress("RedundantOverride", "EmptyMethod")
    override fun performClick(): Boolean {
        return super.performClick()
    }
}
