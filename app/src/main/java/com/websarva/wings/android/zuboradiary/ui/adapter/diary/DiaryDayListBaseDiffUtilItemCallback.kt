package com.websarva.wings.android.zuboradiary.ui.adapter.diary

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.utils.createLogTag

internal abstract class DiaryDayListBaseDiffUtilItemCallback<T : DiaryDayListBaseItem> :
    DiffUtil.ItemCallback<T>() {

    private val logTag = createLogTag()

    override fun areItemsTheSame(
        oldItem: T,
        newItem: T
    ): Boolean {
        Log.d(logTag, "areItemsTheSame()")
        Log.d(logTag, "oldItem_Date = " + oldItem.date)
        Log.d(logTag, "newItem_Date = " + newItem.date)

        return oldItem.areItemsTheSame(newItem)
    }
}
