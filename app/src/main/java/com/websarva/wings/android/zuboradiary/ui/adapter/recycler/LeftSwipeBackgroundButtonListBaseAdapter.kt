package com.websarva.wings.android.zuboradiary.ui.adapter.recycler

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.ui.view.custom.SwipeRecyclerView

internal abstract class LeftSwipeBackgroundButtonListBaseAdapter <T, VH : RecyclerView.ViewHolder> protected constructor(
    recyclerView: SwipeRecyclerView,
    themeColor: ThemeColor,
    diffUtilItemCallback: DiffUtil.ItemCallback<T>
) : ListBaseAdapter<T, VH>(recyclerView, themeColor, diffUtilItemCallback) {

    lateinit var leftSwipeBackgroundButtonSimpleCallback: LeftSwipeBackgroundButtonSimpleCallback
        private set

    fun interface OnBackgroundButtonClickListener<T> {
        fun onClick(item: T)
    }
    protected var onBackgroundButtonClickListener: OnBackgroundButtonClickListener<T>? = null


    constructor(
        recyclerView: SwipeRecyclerView,
        themeColor: ThemeColor,
        diffUtilItemCallback: DiffUtil.ItemCallback<T>,
        leftSwipeBackgroundButtonSimpleCallback: LeftSwipeBackgroundButtonSimpleCallback
    ): this(recyclerView, themeColor, diffUtilItemCallback) {
        this.leftSwipeBackgroundButtonSimpleCallback = leftSwipeBackgroundButtonSimpleCallback
    }

    override fun build() {
        super.build()

        if (this::leftSwipeBackgroundButtonSimpleCallback.isInitialized) {
            leftSwipeBackgroundButtonSimpleCallback.build()
        } else {
            leftSwipeBackgroundButtonSimpleCallback =
                LeftSwipeBackgroundButtonSimpleCallback(recyclerView as SwipeRecyclerView)
                    .apply { build() }
        }
    }

    fun registerOnClickDeleteButtonListener(
        listener: OnBackgroundButtonClickListener<T>
    ) {
        onBackgroundButtonClickListener = listener
    }
}
