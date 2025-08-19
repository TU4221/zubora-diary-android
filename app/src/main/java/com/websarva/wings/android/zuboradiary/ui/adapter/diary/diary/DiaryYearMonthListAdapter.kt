package com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.SwipeDiaryYearMonthListBaseAdapter

internal abstract class DiaryYearMonthListAdapter(
    recyclerView: RecyclerView,
    themeColor: ThemeColor
) : SwipeDiaryYearMonthListBaseAdapter<DiaryYearMonthListItem, DiaryDayListItem>(
    recyclerView,
    themeColor,
    DiffUtilItemCallback()
) {

    override fun createDiaryDayList(
        holder: DiaryYearMonthListViewHolder.Item,
        item: DiaryYearMonthListItem
    ) {
        val diaryDayListAdapter = createDiaryDayListAdapter(holder)
        val diaryDayList = item.diaryDayList.itemList
        diaryDayListAdapter.submitList(diaryDayList)
    }

    private fun createDiaryDayListAdapter(
        holder: DiaryYearMonthListViewHolder.Item,
    ): DiaryDayListAdapter {
        val diaryDayListAdapter =
            DiaryDayListAdapter(holder.binding.recyclerDayList, themeColor)
        return diaryDayListAdapter.apply {
            build()
            registerOnClickItemListener { item: DiaryDayListItem ->
                onClickChildItemListener?.onClick(item)
            }
            registerOnClickDeleteButtonListener { item: DiaryDayListItem ->
                onClickChildItemBackgroundButtonListener?.onClick(item)
            }
        }
    }

    private class DiffUtilItemCallback :
        DiaryYearMonthListBaseAdapter.DiffUtilItemCallback<DiaryYearMonthListItem>() {

        private val logTag = createLogTag()

            override fun areContentsTheSame(
                oldItem: DiaryYearMonthListItem,
                newItem: DiaryYearMonthListItem
            ): Boolean {
                Log.d(
                    logTag,
                    "areContentsTheSame()_oldItem.yearMonth = " + oldItem.yearMonth
                )
                Log.d(
                    logTag,
                    "areContentsTheSame()_newItem.yearMonth = " + newItem.yearMonth
                )

                if (!oldItem.areContentsTheSame(newItem)) {
                    Log.d(logTag, "areContentsTheSame()_不一致")
                    return false
                }

                Log.d(logTag, "areContentsTheSame()_全項目一致")
                return true
            }
    }
}
