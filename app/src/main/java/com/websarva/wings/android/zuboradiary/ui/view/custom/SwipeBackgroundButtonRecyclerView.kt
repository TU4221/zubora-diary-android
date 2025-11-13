package com.websarva.wings.android.zuboradiary.ui.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

// MEMO:下記警告対策として下記クラス作成。
//      ・`OnTouchListener#onTouch()` should call `View#performClick` when a click is detected
//      ・`RecyclerView` has `setOnTouchListener` called on it but does not override `performClick`
//      参照:
internal class SwipeBackgroundButtonRecyclerView : RecyclerView {

    // MEMO:デフォルトスタイル属性 (defStyleAttr) を指定せずにインスタンス化する場合のコンストラクタ。
    //      スーパークラスが自身のデフォルトスタイルを適用する。
    @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
    ) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    private val initializeMotionEvent = null
    private var motionEvent: MotionEvent? = initializeMotionEvent
    private var _previousMotionEventAction = -1

    private var onPerformClickListener: OnPerformClickListener? = null

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        onPerformClickListener = null
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        super.onTouchEvent(e)
        motionEvent = e
        var result = true // MEMO:super.onTouchEvent(e)の戻り値が基本trueの為、performClick()処理以外はtrueを返す
        when (e.action) {
            MotionEvent.ACTION_UP -> {
                if (_previousMotionEventAction == MotionEvent.ACTION_DOWN) {
                    result = performClick()
                }
            }
        }
        updatePreviousMotionEventAction()
        return result
    }

    private fun updatePreviousMotionEventAction() {
        _previousMotionEventAction = motionEvent?.action ?: throw IllegalStateException()
        motionEvent = initializeMotionEvent
    }

    override fun performClick(): Boolean {
        super.performClick()
        val motionEvent = motionEvent ?: throw IllegalStateException()
        return onPerformClickListener?.onPerformClick(this, motionEvent) ?: true
    }

    fun setOnPerformClickListener(listener: OnPerformClickListener?) {
        onPerformClickListener = listener
    }

    fun interface OnPerformClickListener {
        fun onPerformClick(view: View, event: MotionEvent): Boolean
    }
}
