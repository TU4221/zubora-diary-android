package com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.diary

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.ui.model.settings.ThemeColorUi
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.SwipeDiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.diary.list.DiaryYearMonthListItemUi
import com.websarva.wings.android.zuboradiary.ui.view.custom.SwipeRecyclerView
import com.websarva.wings.android.zuboradiary.core.utils.logTag

internal abstract class DiaryYearMonthListAdapter(
    recyclerView: RecyclerView,
    themeColor: ThemeColorUi
) : SwipeDiaryYearMonthListBaseAdapter<DiaryDayListItemUi.Standard>(
    recyclerView,
    themeColor,
    DiffUtilItemCallback()
) {

    override fun createViewHolder(
        parent: ViewGroup,
        themeColorInflater: LayoutInflater,
        viewType: Int
    ): DiaryYearMonthListViewHolder {
        val holder = super.createViewHolder(parent, themeColorInflater, viewType)

        when (holder) {
            is DiaryYearMonthListViewHolder.Item -> {
                applyDiaryDayListAdapter(holder.binding.recyclerDayList)
            }
            is DiaryYearMonthListViewHolder.ProgressBar,
            is DiaryYearMonthListViewHolder.NoDiaryMessage -> {
                // 処理不要
            }
        }

        return holder
    }

    private fun applyDiaryDayListAdapter(
        diaryDayList: SwipeRecyclerView
    ) {
        val diaryDayListAdapter =
            DiaryDayListAdapter(diaryDayList, themeColor)
        diaryDayListAdapter.apply {
            build()
            registerOnItemClickListener { item: DiaryDayListItemUi.Standard ->
                onChildItemClickListener?.onClick(item)
            }
            registerOnClickDeleteButtonListener { item: DiaryDayListItemUi.Standard ->
                onChildItemBackgroundButtonClickListener?.onClick(item)
            }
        }
    }

    override fun bindDiaryDayList(
        holder: DiaryYearMonthListViewHolder.Item,
        item: DiaryYearMonthListItemUi.Diary<DiaryDayListItemUi.Standard>
    ) {
        holder.bindDiaryDayList(item)
    }

    private class DiffUtilItemCallback :
        DiaryYearMonthListBaseAdapter.DiffUtilItemCallback<DiaryDayListItemUi.Standard>() {

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
