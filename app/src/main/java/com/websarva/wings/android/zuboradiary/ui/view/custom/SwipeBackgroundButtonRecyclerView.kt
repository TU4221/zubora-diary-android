package com.websarva.wings.android.zuboradiary.ui.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * スワイプされたアイテムの背景ボタンのクリック判定をサポートするためのカスタムRecyclerView。
 *
 * [ItemTouchHelper]でスワイプされたアイテムのViewは、標準のクリック機能が無効化されてしまう。
 * また、`RecyclerView`に`OnTouchListener`などを設定しても、
 * クリックを判定した際のタッチ座標(`MotionEvent`)をリスナー側で受け取ることが難しい。
 *
 * このクラスは、これらの問題を解決するために存在する。
 * `onTouchEvent`を監視してクリック操作を検出し、その際のタッチ座標情報(`MotionEvent`)と共に
 * [OnPerformClickListener]へ通知する独自のクリック処理を提供する。
 *
 * これにより、リスナー側で「スワイプされたアイテムの、画面上のどの部分（フォアグラウンドか背景ボタンか）が
 * クリックされたか」を正確に判定できるようになる。
 */
// MEMO:下記警告対策として下記クラス作成。
//      ・`OnTouchListener#onTouch()` should call `View#performClick` when a click is detected
//      ・`RecyclerView` has `setOnTouchListener` called on it but does not override `performClick`
//      参照:
internal class SwipeBackgroundButtonRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.recyclerview.R.attr.recyclerViewStyle
) : RecyclerView(context, attrs, defStyleAttr) {

    /** [MotionEvent]を一時的に保持するプロパティの初期値。 */
    private val initializeMotionEvent = null

    /** [onTouchEvent]から[performClick]に渡すための[MotionEvent]。 */
    private var currentMotionEvent: MotionEvent? = initializeMotionEvent

    /** 直前(一つ前の[onTouchEvent])の[MotionEvent]を保持する。 */
    private var previousMotionEvent: MotionEvent? = initializeMotionEvent

    /** クリックイベントを処理するためのリスナー。 */
    private var onPerformClickListener: OnPerformClickListener? = null

    /** 追加処理として、リスナーを解放し、メモリリークを防ぐ。 */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        onPerformClickListener = null
    }

    /**
     * タッチイベントを監視し、
     * クリック操作（`ACTION_DOWN` -> `ACTION_UP`）が検出された場合に`performClick`を呼び出す。
     * @return イベントを処理した場合は`true`。
     */
    override fun onTouchEvent(e: MotionEvent): Boolean {
        super.onTouchEvent(e)
        currentMotionEvent = e
        var result = true // MEMO:super.onTouchEvent(e)の戻り値が基本trueの為、performClick()処理以外はtrueを返す
        when (e.action) {
            MotionEvent.ACTION_UP -> {
                if (previousMotionEvent?.action == MotionEvent.ACTION_DOWN) {
                    result = performClick()
                }
            }
        }
        previousMotionEvent = MotionEvent.obtain(currentMotionEvent)
        currentMotionEvent = initializeMotionEvent
        return result
    }

    /**
     * クリックイベントを発火させ、[onPerformClickListener]に通知する。
     * @return リスナーがイベントを処理した場合はその結果、リスナーが未設定の場合は`true`。
     */
    override fun performClick(): Boolean {
        super.performClick()
        return currentMotionEvent?.let {
            onPerformClickListener?.onPerformClick(this, it) ?: true
        } ?: throw IllegalStateException()
    }

    /**
     * クリックイベントを処理するためのカスタムリスナーを設定する。
     * @param listener 設定するリスナー。不要な場合は`null`。
     */
    fun setOnPerformClickListener(listener: OnPerformClickListener?) {
        onPerformClickListener = listener
    }

    /**
     * [SwipeBackgroundButtonRecyclerView]のクリックイベントを受け取るためのリスナーインターフェース。
     */
    fun interface OnPerformClickListener {
        /**
         * Viewがクリックされたときに呼び出される。
         * @param view クリックされた[SwipeBackgroundButtonRecyclerView]。
         * @param event クリックを構成した`ACTION_UP`イベントの[MotionEvent]。
         * @return イベントが処理された場合は`true`。
         */
        fun onPerformClick(view: SwipeBackgroundButtonRecyclerView, event: MotionEvent): Boolean
    }
}
