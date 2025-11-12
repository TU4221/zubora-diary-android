package com.websarva.wings.android.zuboradiary.ui.recyclerview.helper

import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter.ListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch.BackgroundButtonViewHolder
import com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch.LeftSwipeBackgroundButtonCallback
import com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch.SwipeableViewHolder
import com.websarva.wings.android.zuboradiary.ui.view.custom.SwipeBackgroundButtonRecyclerView

internal class SwipeBackgroundButtonInteractionHelper(
    recyclerView: SwipeBackgroundButtonRecyclerView,
    listAdapter: ListBaseAdapter<*, *>
) :BaseSwipeInteractionHelper<
        SwipeBackgroundButtonRecyclerView,
        ListBaseAdapter<*, *>,
        LeftSwipeBackgroundButtonCallback
>(recyclerView, listAdapter) {

    override fun createLeftSwipeSimpleCallback(): LeftSwipeBackgroundButtonCallback {
        return LeftSwipeBackgroundButtonCallback(
            {
                recyclerView.findViewHolderForAdapterPosition(it)
            },
            {
                itemTouchHelper?.onChildViewDetachedFromWindow(it.itemView)
                itemTouchHelper?.onChildViewAttachedToWindow(it.itemView)
            }
        )
    }

    override fun onSetup() {
        super.onSetup()

        recyclerView.setOnPerformClickListener { view, event ->
            handleSwipedViewHolderClick(view, event)
        }
    }

    override fun onCleanup() {
        super.onCleanup()

        recyclerView.setOnPerformClickListener(null)
    }

    private fun handleSwipedViewHolderClick(v: View, event: MotionEvent): Boolean {
        // タッチViewHolder取得
        val recyclerView = v as? RecyclerView ?: return false
        val childView = recyclerView.findChildViewUnder(event.x, event.y) ?: return false

        // スワイプ中ViewHolder取得
        val adapterPosition = recyclerView.getChildAdapterPosition(childView)
        val viewHolder =
            recyclerView.findViewHolderForAdapterPosition(adapterPosition)
        if (viewHolder !is SwipeableViewHolder) return false
        if (viewHolder !is BackgroundButtonViewHolder) return false

        val tolerance = (3 * v.resources.displayMetrics.density).toInt() // スワイプ位置誤差許容値
        val foregroundView = viewHolder.foregroundView
        val backgroundButtonView = viewHolder.backgroundButtonView

        // アニメーション中無効
        if (viewHolder.isRollingBack) return false
        // スワイプ状態でない
        if (foregroundView.translationX > -backgroundButtonView.width + tolerance) return false

        val rect = Rect()
        backgroundButtonView.getGlobalVisibleRect(rect)
        // 背面ボタン押下時処理
        if (rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
            backgroundButtonView.performClick()
            simpleCallback?.closeSwipedItem()
            return true
        }
        // スワイプアイテム押下時処理
        if (simpleCallback?.swipedAdapterPosition == adapterPosition) {
            simpleCallback?.closeSwipedItem()
            return true
        }
        return false
    }
}
