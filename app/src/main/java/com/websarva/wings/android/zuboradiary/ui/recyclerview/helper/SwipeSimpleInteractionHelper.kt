package com.websarva.wings.android.zuboradiary.ui.recyclerview.helper

import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.recyclerview.adapter.ListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.recyclerview.callback.touch.SimpleLeftSwipeCallback

internal class SwipeSimpleInteractionHelper(
    recyclerView: RecyclerView,
    listAdapter: ListBaseAdapter<*, *>,
    private val handleOnSwiped: (Int) -> Unit = {}
) : BaseSwipeInteractionHelper<
        RecyclerView,
        ListBaseAdapter<*, *>,
        SimpleLeftSwipeCallback
>(recyclerView, listAdapter) {

    override fun createLeftSwipeSimpleCallback(): SimpleLeftSwipeCallback {
        return SimpleLeftSwipeCallback(
            {
                recyclerView.findViewHolderForAdapterPosition(it)
            },
            {
                itemTouchHelper?.onChildViewDetachedFromWindow(it.itemView)
                itemTouchHelper?.onChildViewAttachedToWindow(it.itemView)
            },
            handleOnSwiped
        )
    }
}
