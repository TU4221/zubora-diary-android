package com.websarva.wings.android.zuboradiary.ui.adapter.diary.diary

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.data.model.ThemeColor
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryDayListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.DiaryYearMonthListBaseItem
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.SwipeDiaryDayListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.diary.SwipeDiaryYearMonthListBaseAdapter

internal abstract class DiaryYearMonthListAdapter(
    recyclerView: RecyclerView,
    themeColor: ThemeColor
) : SwipeDiaryYearMonthListBaseAdapter(recyclerView, themeColor, DiffUtilItemCallback()) {

    override fun createDiaryDayList(
        holder: DiaryYearMonthListViewHolder,
        item: DiaryYearMonthListBaseItem
    ) {
        if (item !is DiaryYearMonthListItem) throw IllegalStateException()

        val diaryDayListAdapter = createDiaryDayListAdapter(holder)
        val diaryDayList = item.diaryDayList.itemList
        val convertedList: List<DiaryDayListBaseItem> = diaryDayList
        diaryDayListAdapter.submitList(convertedList)
    }

    private fun createDiaryDayListAdapter(
        holder: DiaryYearMonthListViewHolder
    ): DiaryDayListAdapter {
        val diaryDayListAdapter =
            DiaryDayListAdapter(holder.binding.recyclerDayList, themeColor)
        return diaryDayListAdapter.apply {
            build()
            onClickItemListener =
                DiaryDayListBaseAdapter.OnClickItemListener { item: DiaryDayListBaseItem ->
                    onClickChildItemListener?.onClick(item) ?: return@OnClickItemListener
                }
            onClickDeleteButtonListener =
                SwipeDiaryDayListBaseAdapter.OnClickDeleteButtonListener { item: DiaryDayListBaseItem ->
                    onClickChildItemBackgroundButtonListener?.onClick(item)
                        ?: return@OnClickDeleteButtonListener
                }
        }
    }

    private class DiffUtilItemCallback : DiaryYearMonthListBaseAdapter.DiffUtilItemCallback() {

        private val logTag = createLogTag()

            override fun areContentsTheSame(
                oldItem: DiaryYearMonthListBaseItem,
                newItem: DiaryYearMonthListBaseItem
            ): Boolean {
                Log.d(
                    logTag,
                    "areContentsTheSame()_oldItem.yearMonth = " + oldItem.yearMonth
                )
                Log.d(
                    logTag,
                    "areContentsTheSame()_newItem.yearMonth = " + newItem.yearMonth
                )

                if (oldItem !is DiaryYearMonthListItem) throw IllegalStateException()
                if (newItem !is DiaryYearMonthListItem) throw IllegalStateException()

                if (!oldItem.areContentsTheSame(newItem)) {
                    Log.d(logTag, "areContentsTheSame()_不一致")
                    return false
                }

                Log.d(logTag, "areContentsTheSame()_全項目一致")
                return true
            }
    }
}
