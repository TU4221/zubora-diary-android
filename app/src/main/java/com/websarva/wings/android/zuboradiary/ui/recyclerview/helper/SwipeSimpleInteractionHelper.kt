package com.websarva.wings.android.zuboradiary.ui.recyclerview.helper

import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter.ListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch.SimpleLeftSwipeCallback

/**
 * RecyclerViewにシンプルな左スワイプ操作のインタラクションをセットアップするためのヘルパークラス。
 *
 * [BaseSwipeInteractionHelper]を継承し、[SimpleLeftSwipeCallback]を使用してスワイプ機能を実装する。
 *
 * @param recyclerView インタラクションを適用するRecyclerView。
 * @param listAdapter 対象となるRecyclerViewのアダプター。
 * @param onSwiped アイテムが完全にスワイプされたときに実行されるコールバック。
 */
internal class SwipeSimpleInteractionHelper(
    recyclerView: RecyclerView,
    listAdapter: ListBaseAdapter<*, *>,
    private val onSwiped: (Int) -> Unit = {}
) : BaseSwipeInteractionHelper<
        RecyclerView,
        ListBaseAdapter<*, *>,
        SimpleLeftSwipeCallback
>(recyclerView, listAdapter) {

    /**
     * [SimpleLeftSwipeCallback]のインスタンスを生成して返す。
     * @return 生成された[SimpleLeftSwipeCallback]のインスタンス。
     */
    override fun createLeftSwipeSimpleCallback(): SimpleLeftSwipeCallback {
        return SimpleLeftSwipeCallback(
            {
                recyclerView.findViewHolderForAdapterPosition(it)
            },
            {
                itemTouchHelper?.onChildViewDetachedFromWindow(it.itemView)
                itemTouchHelper?.onChildViewAttachedToWindow(it.itemView)
            },
            onSwiped
        )
    }
}
