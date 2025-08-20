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
        val result =
            if (oldItem === newItem) {
                true
            } else {
                oldItem.date == newItem.date
            }

        Log.d(
            logTag,
            "areItemsTheSame()_result = ${result}_oldItem = ${oldItem}_newItem = $newItem"
        )
        return result
    }
}
