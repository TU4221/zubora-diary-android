package com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch

import androidx.recyclerview.widget.RecyclerView

internal open class SimpleLeftSwipeCallback(
    processToFindViewHolder: (position: Int) -> RecyclerView.ViewHolder?,
    processReattachAfterCloseAnimation: (RecyclerView.ViewHolder) -> Unit,
    onSwiped: (position: Int) -> Unit
) : BaseLeftSwipeCallback(
    processToFindViewHolder,
    processReattachAfterCloseAnimation,
    onSwiped
) {

    override fun drawViewHolder(viewHolder: RecyclerView.ViewHolder, dX: Float) {
        val leftSwipeViewHolder = viewHolder as? SwipeableViewHolder ?: return
        leftSwipeViewHolder.foregroundView.translationX = dX
    }
}
