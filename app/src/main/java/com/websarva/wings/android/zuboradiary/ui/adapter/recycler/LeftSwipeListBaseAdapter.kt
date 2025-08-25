package com.websarva.wings.android.zuboradiary.ui.adapter.recycler

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.view.custom.SwipeRecyclerView

internal abstract class LeftSwipeListBaseAdapter <T, VH : RecyclerView.ViewHolder> protected constructor(
    recyclerView: SwipeRecyclerView,
    themeColor: ThemeColor,
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

    override fun clearViewBindings() {
        leftSwipeSimpleCallback.clearViewBindings()
        onItemSwipeListener = null

        super.clearViewBindings()
    }

    fun registerOnItemSwipeListener(
        listener: OnItemSwipeListener<T>
    ) {
        onItemSwipeListener = listener
    }
}
