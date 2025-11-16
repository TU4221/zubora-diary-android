package com.websarva.wings.android.zuboradiary.ui.recyclerview.decoration

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.R
import com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter.DiaryListBaseAdapter

internal class SpacingItemDecoration(
    context: Context
) : RecyclerView.ItemDecoration() {
    private val itemSpacing = context.resources.getDimensionPixelSize(R.dimen.row_diary_list_layout_margin_vertical_between_items)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        // ViewHolderのポジションを取得
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val adapter = parent.adapter as? DiaryListBaseAdapter<*, *> ?: return

        if (position == 0) {
            // リスト最初のヘッダー(またはアイテム)のtopのmarginは0
            outRect.top = 0
        } else {
            if (adapter.isSpacingItem(position)) {
                // positionが1以上の全てのアイテムの上側に、統一の間隔を設定する
                outRect.top = itemSpacing
            }
            if (!adapter.isSpacingItem(position + 1)) {
                // positionが1以上の全てのアイテムの上側に、統一の間隔を設定する
                outRect.bottom = itemSpacing
            }
        }
    }
}
