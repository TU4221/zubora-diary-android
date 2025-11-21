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
    private val itemSpacing =
        context.resources.getDimensionPixelSize(
            R.dimen.row_diary_list_layout_margin_vertical_between_items
        )

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

        if (position == 0) {
            // リスト最初のヘッダー(またはアイテム)のtopのmarginは0
            outRect.top = 0
        } else {
            if (provider.isSpacingItem(position)) {
                // positionが1以上の全てのアイテムの上側に、統一の間隔を設定する
                outRect.top = itemSpacing
            }
            if (!provider.isSpacingItem(position + 1)) {
                // positionが1以上の全てのアイテムの上側に、統一の間隔を設定する
                outRect.bottom = itemSpacing
            }
        }
    }
}
