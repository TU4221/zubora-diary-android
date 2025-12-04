package com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch

import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.core.utils.logTag
import java.lang.IllegalArgumentException

/**
 * RecyclerViewのアイテムに対する左スワイプで、
 * 背景にボタンを表示する機能を実装するための[ItemTouchHelper.Callback]。
 *
 * 以下の責務を持つ:
 * - スワイプの閾値を、背面ボタンの幅に基づいて動的に計算する。
 * - スワイプ完了時に、背面ボタンが完全に表示される位置までのオフセットを計算・保持する。
 * - スワイプ中の描画で、フォアグラウンドビューの移動量を背面ボタンの幅までに制限し、ボタンが隠れないように制御する。
 * - ViewHolderが[SwipeableViewHolder]と[BackgroundButtonViewHolder]の両方を実装していることを要求する。
 *
 * @param processToFindViewHolder 指定されたポジションのViewHolderを見つけるための関数。
 * @param processReattachAfterCloseAnimation クローズアニメーションの完了後に、
 * ViewHolderをItemTouchHelperに再アタッチするためのコールバック。
 */
internal open class LeftSwipeBackgroundButtonCallback(
    processToFindViewHolder: (Int) -> RecyclerView.ViewHolder?,
    processReattachAfterCloseAnimation: (RecyclerView.ViewHolder) -> Unit
) : BaseLeftSwipeCallback(
    processToFindViewHolder,
    processReattachAfterCloseAnimation
) {

    /**
     * スワイプ完了後に、`ItemTouchHelper`が提供する`dX`の値を補正するためのオフセット値。
     *
     *`ItemTouchHelper`はスワイプ完了時、`dX`をViewHolderの幅全体として扱う。
     * 一方、このクラスはフォアグラウンドビューの移動を背面ボタンの幅に制限しているため、
     * 両者の座標系にズレが生じる。
     *
     * この`swipingOffset`は、そのズレを吸収し、スワイプ完了後もビューが正しい位置に
     * 描画されるようにするために、[drawViewHolder]内で`dX`に加算される。
     */
    private var swipingOffset: Float = 0f

    /** スワイプアクションを完了とみなすための閾値を背面ボタンの幅に基づいて計算し、返す。。 */
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

    /** スワイプが完了した際に、描画オフセットを計算する。 */
    override fun onSwipedHook(viewHolder: RecyclerView.ViewHolder) {
        val recyclerView =
            viewHolder.itemView.parent as? RecyclerView ?: throw IllegalArgumentException()
        if (viewHolder !is BackgroundButtonViewHolder) return

        swipingOffset =
            (recyclerView.width - viewHolder.backgroundButtonView.width).toFloat()
    }

    /** フォアグラウンドビューの移動量を背面ボタンの幅に制限して描画する。 */
    override fun drawViewHolder(viewHolder: RecyclerView.ViewHolder, dX: Float) {
        if (viewHolder !is SwipeableViewHolder) return
        if (viewHolder !is BackgroundButtonViewHolder) return

        val backgroundButtonWidth = viewHolder.backgroundButtonView.width.toFloat()
        val currentDx = if (swipedAdapterPosition == viewHolder.bindingAdapterPosition) {
            dX + swipingOffset
        } else {
            dX
        }
        viewHolder.foregroundView.translationX =  currentDx.coerceIn(-backgroundButtonWidth, 0.0f)
    }
}
