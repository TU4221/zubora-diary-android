package com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal abstract class DiaryDayListBaseDiffUtilItemCallback<T : DiaryDayListItem> :
    DiffUtil.ItemCallback<T>() {

    private val logTag = createLogTag()

    override fun areItemsTheSame(
        oldItem: T,
        newItem: T
    ): Boolean {
        Log.d(logTag, "areItemsTheSame()")
        Log.d(logTag, "oldItem_Date = " + oldItem.date)
        Log.d(logTag, "newItem_Date = " + newItem.date)

        if (oldItem === newItem) return true

        return oldItem.date == newItem.date
    }
}
