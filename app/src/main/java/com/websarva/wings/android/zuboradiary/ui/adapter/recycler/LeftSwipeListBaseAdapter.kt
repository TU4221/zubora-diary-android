package com.websarva.wings.android.zuboradiary.ui.adapter.recycler

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.view.custom.SwipeRecyclerView

internal abstract class LeftSwipeListBaseAdapter <T, VH : RecyclerView.ViewHolder> protected constructor(
    recyclerView: SwipeRecyclerView,
    themeColor: ThemeColorUi,
    diffUtilItemCallback: DiffUtil.ItemCallback<T>
) : ListBaseAdapter<T, VH>(recyclerView, themeColor, diffUtilItemCallback) {

    protected lateinit var leftSwipeSimpleCallback: LeftSwipeSimpleCallback

    fun interface OnItemSwipeListener<T> {
        fun onSwipe(item: T)
    }
    protected var onItemSwipeListener: OnItemSwipeListener<T>? = null

    override fun build() {
        super.build()

        leftSwipeSimpleCallback = LeftSwipeSimpleCallback(recyclerView as SwipeRecyclerView)
        leftSwipeSimpleCallback.build()
    }

    fun registerOnItemSwipeListener(
        listener: OnItemSwipeListener<T>
    ) {
        onItemSwipeListener = listener
    }
}
