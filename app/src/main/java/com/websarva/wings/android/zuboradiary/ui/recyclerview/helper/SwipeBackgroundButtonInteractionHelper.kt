package com.websarva.wings.android.zuboradiary.ui.recyclerview.helper

import android.graphics.Rect
import android.view.MotionEvent
import com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter.ListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch.BackgroundButtonViewHolder
import com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch.LeftSwipeBackgroundButtonCallback
import com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch.SwipeableViewHolder
import com.websarva.wings.android.zuboradiary.ui.view.custom.SwipeBackgroundButtonRecyclerView

/**
 * [SwipeBackgroundButtonRecyclerView]に、
 * 左スワイプで背景ボタンを表示するインタラクションをセットアップするためのヘルパークラス。
 *
 * [BaseSwipeInteractionHelper]を継承し、以下の機能を提供する:
 * - [LeftSwipeBackgroundButtonCallback]を使用して、スワイプと背景ボタンの表示を実装する。
 * - [SwipeBackgroundButtonRecyclerView]にリスナーをセットアップし、
 *   スワイプされたアイテムや背景ボタンがクリックされた際の処理をハンドリングする。
 *
 * @param recyclerView インタラクションを適用する[SwipeBackgroundButtonRecyclerView]。
 * @param listAdapter 対象となるRecyclerViewのアダプター。
 */
internal class SwipeBackgroundButtonInteractionHelper(
    recyclerView: SwipeBackgroundButtonRecyclerView,
    listAdapter: ListBaseAdapter<*, *>
) :BaseSwipeInteractionHelper<
        SwipeBackgroundButtonRecyclerView,
        ListBaseAdapter<*, *>,
        LeftSwipeBackgroundButtonCallback
>(recyclerView, listAdapter) {

    /** [LeftSwipeBackgroundButtonCallback]のインスタンスを生成して返す。 */
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

    /** RecyclerViewにクリックリスナーをセットアップする。 */
    override fun onSetup() {
        super.onSetup()

        recyclerView.setOnPerformClickListener { view, event ->
            handleSwipedViewHolderClick(view, event)
        }
    }

    /** セットアップしたクリックリスナーを解放する。 */
    override fun onCleanup() {
        super.onCleanup()

        recyclerView.setOnPerformClickListener(null)
    }

    /**
     * スワイプされた状態のアイテムに対するクリックイベントを処理する。
     *
     * 背景ボタンの領域がクリックされた場合はボタンの`performClick`を呼び出し、
     * それ以外のフォアグラウンド領域がクリックされた場合はスワイプ状態を閉じる。
     *
     * @param recyclerView タッチイベントが発生した[SwipeBackgroundButtonRecyclerView]。
     * @param event 発生した[MotionEvent]。
     * @return イベントを消費した場合は`true`。
     */
    private fun handleSwipedViewHolderClick(
        recyclerView: SwipeBackgroundButtonRecyclerView,
        event: MotionEvent
    ): Boolean {
        // タッチViewHolder取得
        val childView = recyclerView.findChildViewUnder(event.x, event.y) ?: return false

        // スワイプ中ViewHolder取得
        val adapterPosition = recyclerView.getChildAdapterPosition(childView)
        val viewHolder =
            recyclerView.findViewHolderForAdapterPosition(adapterPosition)
        if (viewHolder !is SwipeableViewHolder) return false
        if (viewHolder !is BackgroundButtonViewHolder) return false

        val tolerance = (3 * recyclerView.resources.displayMetrics.density).toInt() // スワイプ位置誤差許容値
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
