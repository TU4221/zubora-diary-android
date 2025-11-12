package com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import kotlin.math.max
import kotlin.math.min

internal open class LeftSwipeBackgroundButtonCallback(
    processToFindViewHolder: (Int) -> RecyclerView.ViewHolder?,
    processReattachAfterCloseAnimation: (RecyclerView.ViewHolder) -> Unit
) : BaseLeftSwipeCallback(
    processToFindViewHolder,
    processReattachAfterCloseAnimation
) {

    private var swipingOffset: Float = 0f

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        Log.d(logTag, "getSwipeThreshold()_position = " + viewHolder.bindingAdapterPosition)
        if (viewHolder !is BackgroundButtonViewHolder) return super.getSwipeThreshold(viewHolder)

        // スワイプ境界を背面ボタンの中心にする
        val recyclerView =
            viewHolder.itemView.parent as? RecyclerView ?: return super.getSwipeThreshold(viewHolder)
        val threshold =
            viewHolder.backgroundButtonView.width / 2f / recyclerView.width

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

    override fun onSwipedHook(viewHolder: RecyclerView.ViewHolder) {
        val recyclerView = viewHolder.itemView.parent as? RecyclerView ?: return
        if (viewHolder !is BackgroundButtonViewHolder) return

        swipingOffset =
            (recyclerView.width - viewHolder.backgroundButtonView.width).toFloat()
    }

    override fun drawViewHolder(viewHolder: RecyclerView.ViewHolder, dX: Float) {
        if (viewHolder !is SwipeableViewHolder) return
        if (viewHolder !is BackgroundButtonViewHolder) return

        val backgroundButtonWidth = viewHolder.backgroundButtonView.width.toFloat()
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
        viewHolder.foregroundView.translationX = translationValueX
    }
}
