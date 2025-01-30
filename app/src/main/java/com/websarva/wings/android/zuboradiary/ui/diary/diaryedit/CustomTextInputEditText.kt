package com.websarva.wings.android.zuboradiary.ui.diary.diaryedit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;

/**
 * 本クラスは”TextInputEditText”をタッチした時、一度のタッチで"OnClickListener"が処理されるようにカスタマイズしたクラスである。
 * ”TextInputLayout”に"OnClickListener"を設定しても処理はされない。
 * */

// MEMO:"TextInputEditText"にOnTouchListenerをセットすると
//      警告"Custom view has setOnTouchListener called on it but does not override performClick"が表示される。
//      これは本来のアクセシビリティサービスが損なわれる可能性があることを示唆している。
//      対策としてonTouchEvent()メソッドをOverrideした本クラスを作成して使用。
//      https://www.binarydevelop.com/article/androidsetontouchlistenerperformclick-68009
public class CustomTextInputEditText extends TextInputEditText {
    public CustomTextInputEditText(@NonNull Context context) {
        super(context);
    }

    public CustomTextInputEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextInputEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) performClick();
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
