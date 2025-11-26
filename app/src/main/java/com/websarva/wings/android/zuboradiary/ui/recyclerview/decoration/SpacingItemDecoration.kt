package com.websarva.wings.android.zuboradiary.ui.recyclerview.decoration

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.R

/**
 * RecyclerViewの特定のアイテム間に、垂直方向の間隔を設定するための[RecyclerView.ItemDecoration]。
 *
 * [SpacingItemProvider]を実装した[RecyclerView.Adapter]と連携して動作する。
 * [SpacingItemProvider.isSpacingItem]が`true`を返すアイテムの上部に、指定された間隔を追加する。
 *
 * @param context リソースを取得するためのコンテキスト。
 */
internal class SpacingItemDecoration(
    context: Context
) : RecyclerView.ItemDecoration() {

    /** アイテム間に適用される垂直方向の間隔（ピクセル単位）。 */
    private val itemSpacing = context.resources.getDimensionPixelSize(R.dimen.recycler_item_spacing_vertical)

    /** 各アイテムのオフセット（マージン）を設定する。 */
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        // ViewHolderのポジションを取得
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val provider = parent.adapter as? SpacingItemProvider ?: return

        if (provider.isSpacingItem(position)) {
            outRect.bottom = itemSpacing
        }

    }
}
