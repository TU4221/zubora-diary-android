package com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.diary

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.SwipeDiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItem
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.diary.DiaryDayList
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryYearMonthListItem

internal abstract class DiaryYearMonthListAdapter(
    recyclerView: RecyclerView,
    themeColor: ThemeColor
) : SwipeDiaryYearMonthListBaseAdapter<DiaryDayList, DiaryDayListItem.Standard>(
    recyclerView,
    themeColor,
    DiffUtilItemCallback()
) {

    override fun createDiaryDayList(
        holder: DiaryYearMonthListViewHolder.Item,
        item: DiaryYearMonthListItem.Diary<DiaryDayList>
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
            registerOnItemClickListener { item: DiaryDayListItem.Standard ->
                onChildItemClickListener?.onClick(item)
            }
            registerOnClickDeleteButtonListener { item: DiaryDayListItem.Standard ->
                onChildItemBackgroundButtonClickListener?.onClick(item)
            }
        }
    }

    private class DiffUtilItemCallback :
        DiaryYearMonthListBaseAdapter.DiffUtilItemCallback<DiaryDayList>() {

        private val logTag = createLogTag()

            override fun areContentsTheSame(
                oldItem: DiaryYearMonthListItem<DiaryDayList>,
                newItem: DiaryYearMonthListItem<DiaryDayList>
            ): Boolean {
                val result =
                    when (oldItem) {
                        is DiaryYearMonthListItem.Diary -> {
                            if (newItem !is DiaryYearMonthListItem.Diary<DiaryDayList>) {
                                false
                            } else {
                                oldItem.diaryDayList == newItem.diaryDayList
                            }
                        }
                        is DiaryYearMonthListItem.NoDiaryMessage,
                        is DiaryYearMonthListItem.ProgressIndicator -> {
                            false
                        }
                    }

                Log.d(
                    logTag,
                    "areContentsTheSame()_result = ${result}_oldItem = ${oldItem}_newItem = $newItem"
                )
                return result
            }
    }
}
