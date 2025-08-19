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

    fun interface OnSwipeListener<T> {
        fun onSwipe(item: T)
    }
    protected var onSwipeListener: OnSwipeListener<T>? = null

    override fun build() {
        super.build()

        leftSwipeSimpleCallback = LeftSwipeSimpleCallback(recyclerView as SwipeRecyclerView)
        leftSwipeSimpleCallback.build()
    }

    override fun clearViewBindings() {
        super.clearViewBindings()

        leftSwipeSimpleCallback.clearViewBindings()
        onSwipeListener = null
    }

    fun registerOnSwipeListener(
        onSwipeListener: OnSwipeListener<T>
    ) {
        this.onSwipeListener = onSwipeListener
    }
}
