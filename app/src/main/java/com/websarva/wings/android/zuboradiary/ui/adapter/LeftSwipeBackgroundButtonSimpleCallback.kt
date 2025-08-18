package com.websarva.wings.android.zuboradiary.ui.adapter

import android.graphics.Rect
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.view.custom.SwipeRecyclerView
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import kotlin.math.max
import kotlin.math.min

internal open class LeftSwipeBackgroundButtonSimpleCallback(recyclerView: SwipeRecyclerView) :
    LeftSwipeSimpleCallback(recyclerView) {

    private val logTag = createLogTag()

    private var swipingOffset: Float = 0f

    var isSwipeEnabled = true

    override fun build() {
        super.build()
        recyclerView.setOnPerformClickListener { view, event ->
            onClickSwipedViewHolder(view, event)
        }
    }

    private fun onClickSwipedViewHolder(v: View, event: MotionEvent): Boolean {
        // タッチViewHolder取得
        val childView = recyclerView.findChildViewUnder(event.x, event.y) ?: return false

        val adapterPosition = recyclerView.getChildAdapterPosition(childView)
        val viewHolder =
            recyclerView.findViewHolderForAdapterPosition(adapterPosition)
        val leftSwipeViewHolder = viewHolder as LeftSwipeViewHolder<*>

        val tolerance = (3 * v.resources.displayMetrics.density).toInt() // スワイプ位置誤差許容値
        val foregroundView = leftSwipeViewHolder.foregroundView
        val backgroundButtonView = leftSwipeViewHolder.backgroundButtonView

        // アニメーション中無効
        if (!foregroundView.isClickable) return false
        // スワイプ状態でない
        if (foregroundView.translationX > -backgroundButtonView.width + tolerance) return false

        val rect = Rect()
        backgroundButtonView.getGlobalVisibleRect(rect)
        // 背面ボタン押下時処理
        if (rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
            backgroundButtonView.performClick()
            closeSwipedViewHolder(adapterPosition)
            return true
        }
        // スワイプアイテム押下時処理
        if (swipedAdapterPosition == adapterPosition) {
            closeSwipedViewHolder(adapterPosition)
            return true
        }
        return false
    }

    // MEMO:スワイプ時、タッチ状態を継続したままRecyclerViewを更新するとonSwiped()が起動するが、
    //      対象ViewHolderのItemPositionが-1となるため、Overrideで記述したコードで例外が発生する。
    //      その為、RecyclerViewを更新時はgetSwipeDirs()をOverrideしてスワイプ機能を無効にする。
    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return if (isSwipeEnabled) {
            super.getSwipeDirs(recyclerView, viewHolder)
        } else {
            0
        }
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        Log.d(logTag, "getSwipeThreshold()_position = " + viewHolder.bindingAdapterPosition)
        val leftSwipeViewHolder = viewHolder as LeftSwipeViewHolder<*>

        // スワイプ境界を背面ボタンの中心にする
        val threshold =
            leftSwipeViewHolder.backgroundButtonView.width / 2f / recyclerView.width

        // スワイプメニューを閉じる時は、反対方向からの割合に変更
        // MEMO:ViewHolderの前面Viewは背面ボタン位置までのスワイプ状態になっているが、
        //      スワイプ機能の値(ItemTouchHelper.Callback#onChildDraw()の引数であるdX)としては
        //      ViewHolderの端までスワイプしている事になっている。その為下記コードが必要となる。
        if (swipedAdapterPosition != viewHolder.getBindingAdapterPosition()) {
            Log.d(logTag, "getSwipeThreshold()_return = $threshold")
            return threshold
        }

        Log.d(logTag, "getSwipeThreshold()_return = " + (1 - threshold))
        return 1 - threshold
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        Log.d(logTag, "onSwiped()_position = " + viewHolder.bindingAdapterPosition)
        if (direction != ItemTouchHelper.LEFT) return

        val leftSwipeViewHolder = viewHolder as LeftSwipeViewHolder<*>

        if (swipingAdapterPosition != viewHolder.getBindingAdapterPosition()) return
        swipedAdapterPosition = swipingAdapterPosition
        clearSwipingAdapterPosition()
        swipingOffset =
            (recyclerView.width - leftSwipeViewHolder.backgroundButtonView.width).toFloat()
    }

    override fun translateForegroundView(
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        actionState: Int
    ) {
        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return

        val leftSwipeViewHolder = viewHolder as LeftSwipeViewHolder<*>
        val backgroundButtonWidth =
            leftSwipeViewHolder.backgroundButtonView.width.toFloat()
        val translationValueX =
            if (swipedAdapterPosition == viewHolder.getBindingAdapterPosition()) {
                min(
                    0.0,
                    max(-backgroundButtonWidth.toDouble(), (dX + swipingOffset).toDouble())
                ).toFloat()
            } else {
                min(
                    0.0,
                    max(-backgroundButtonWidth.toDouble(), dX.toDouble())
                ).toFloat()
            }
        Log.d(logTag, "onChildDraw()_translationValueX = $translationValueX")
        leftSwipeViewHolder.foregroundView.translationX = translationValueX
    }
}
