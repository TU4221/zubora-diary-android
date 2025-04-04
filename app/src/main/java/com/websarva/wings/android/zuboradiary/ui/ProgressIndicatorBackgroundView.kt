package com.websarva.wings.android.zuboradiary.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ProgressIndicatorBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    init {
        setUpOnTouchListener()
    }

    // 画面全体ProgressBar表示中はタッチ無効化
    private fun setUpOnTouchListener() {
        setOnTouchListener { v: View, _: MotionEvent ->
            v.performClick()
            true
        }
    }
}
