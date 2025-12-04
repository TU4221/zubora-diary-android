package com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalArgumentException

/**
 * RecyclerViewのアイテムに対するシンプルな左スワイプ機能を実装するための[ItemTouchHelper.Callback]。
 *
 * このクラスは、フォアグラウンドビューをスワイプされた距離と一致させる、基本的なスワイプ描画を行う。
 *
 * @param processToFindViewHolder 指定されたポジションのViewHolderを見つけるための関数。
 * @param processReattachAfterCloseAnimation クローズアニメーションの完了後に、ViewHolderをItemTouchHelperに再アタッチするためのコールバック。
 * @param onSwiped アイテムが完全にスワイプされたときに呼び出されるコールバック。
 */
internal open class SimpleLeftSwipeCallback(
    processToFindViewHolder: (position: Int) -> RecyclerView.ViewHolder?,
    processReattachAfterCloseAnimation: (RecyclerView.ViewHolder) -> Unit,
    onSwiped: (position: Int) -> Unit
) : BaseLeftSwipeCallback(
    processToFindViewHolder,
    processReattachAfterCloseAnimation,
    onSwiped
) {

    /** [BaseLeftSwipeCallback]の実装。フォアグラウンドビューのX方向の移動量を、スワイプされた距離`dX`に設定する。 */
    override fun drawViewHolder(viewHolder: RecyclerView.ViewHolder, dX: Float) {
        val leftSwipeViewHolder =
            viewHolder as? SwipeableViewHolder ?: throw IllegalArgumentException()
        leftSwipeViewHolder.foregroundView.translationX = dX
    }
}
