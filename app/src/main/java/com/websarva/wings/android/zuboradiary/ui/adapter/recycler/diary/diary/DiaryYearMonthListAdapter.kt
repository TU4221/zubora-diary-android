package com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.diary

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.SwipeDiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryYearMonthListItemUi

internal abstract class DiaryYearMonthListAdapter(
    recyclerView: RecyclerView,
    themeColor: ThemeColor
) : SwipeDiaryYearMonthListBaseAdapter<DiaryDayListItemUi.Standard>(
    recyclerView,
    themeColor,
    DiffUtilItemCallback()
) {

    override fun createDiaryDayList(
        holder: DiaryYearMonthListViewHolder.Item,
        item: DiaryYearMonthListItemUi.Diary<DiaryDayListItemUi.Standard>
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
            registerOnItemClickListener { item: DiaryDayListItemUi.Standard ->
                onChildItemClickListener?.onClick(item)
            }
            registerOnClickDeleteButtonListener { item: DiaryDayListItemUi.Standard ->
                onChildItemBackgroundButtonClickListener?.onClick(item)
            }
        }
    }

    private class DiffUtilItemCallback :
        DiaryYearMonthListBaseAdapter.DiffUtilItemCallback<DiaryDayListItemUi.Standard>() {

        private val logTag = createLogTag()

            override fun areContentsTheSame(
                oldItem: DiaryYearMonthListItemUi<DiaryDayListItemUi.Standard>,
                newItem: DiaryYearMonthListItemUi<DiaryDayListItemUi.Standard>
            ): Boolean {
                val result =
                    when (oldItem) {
                        is DiaryYearMonthListItemUi.Diary -> {
                            if (newItem !is DiaryYearMonthListItemUi.Diary<DiaryDayListItemUi.Standard>) {
                                false
                            } else {
                                oldItem.diaryDayList == newItem.diaryDayList
                            }
                        }
                        is DiaryYearMonthListItemUi.NoDiaryMessage,
                        is DiaryYearMonthListItemUi.ProgressIndicator -> {
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
