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

    fun interface OnBackgroundButtonClickListener<T> {
        fun onClick(item: T)
    }
    protected var onBackgroundButtonClickListener: OnBackgroundButtonClickListener<T>? = null

    override fun clearViewBindings() {
        super.clearViewBindings()

        onBackgroundButtonClickListener = null
    }

    fun registerOnClickDeleteButtonListener(
        listener: OnBackgroundButtonClickListener<T>
    ) {
        onBackgroundButtonClickListener = listener
    }
}
