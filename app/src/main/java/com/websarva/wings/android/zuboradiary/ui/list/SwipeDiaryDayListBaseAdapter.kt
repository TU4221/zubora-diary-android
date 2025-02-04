package com.websarva.wings.android.zuboradiary.ui.list

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.data.preferences.ThemeColor

abstract class SwipeDiaryDayListBaseAdapter protected constructor(
    context: Context,
    recyclerView: RecyclerView,
    themeColor: ThemeColor,
    diffUtilItemCallback: DiffUtilItemCallback
) : DiaryDayListBaseAdapter(context, recyclerView, themeColor, diffUtilItemCallback) {

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
