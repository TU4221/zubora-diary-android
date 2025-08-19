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

    fun interface OnClickDeleteButtonListener<T> {
        fun onClick(item: T)
    }
    protected var onClickDeleteButtonListener: OnClickDeleteButtonListener<T>? = null

    override fun clearViewBindings() {
        super.clearViewBindings()

        onClickDeleteButtonListener = null
    }

    fun registerOnClickDeleteButtonListener(
        onClickDeleteButtonListener: OnClickDeleteButtonListener<T>
    ) {
        this.onClickDeleteButtonListener = onClickDeleteButtonListener
    }
}
