package com.websarva.wings.android.zuboradiary.ui.adapter.diary

import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor

internal abstract class SwipeDiaryDayListBaseAdapter protected constructor(
    recyclerView: RecyclerView,
    themeColor: ThemeColor,
    diffUtilItemCallback: DiffUtilItemCallback
) : DiaryDayListBaseAdapter(recyclerView, themeColor, diffUtilItemCallback) {

    fun interface OnClickDeleteButtonListener {
        fun onClick(item: DiaryDayListBaseItem)
    }
    var onClickDeleteButtonListener: OnClickDeleteButtonListener? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val item = getItem(position)
        onBindDeleteButtonClickListener(holder, item)
    }

    protected abstract fun onBindDeleteButtonClickListener(
        holder: RecyclerView.ViewHolder,
        item: DiaryDayListBaseItem
    )

    protected fun onClickDeleteButton(item: DiaryDayListBaseItem) {
        onClickDeleteButtonListener?.onClick(item) ?: return
    }
}
