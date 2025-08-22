package com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.wordsearchresult

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.websarva.wings.android.zuboradiary.domain.model.ThemeColor
import com.websarva.wings.android.zuboradiary.utils.createLogTag
import com.websarva.wings.android.zuboradiary.ui.adapter.recycler.diary.DiaryYearMonthListBaseAdapter
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryDayListItemUi
import com.websarva.wings.android.zuboradiary.ui.model.list.diary.DiaryYearMonthListItemUi

internal abstract class WordSearchResultYearMonthListAdapter(
    recyclerView: RecyclerView,
    themeColor: ThemeColor
) : DiaryYearMonthListBaseAdapter<DiaryDayListItemUi.WordSearchResult>(
    recyclerView,
    themeColor,
    DiffUtilItemCallback()
) {

    override fun createDiaryDayList(
        holder: DiaryYearMonthListViewHolder.Item,
        item: DiaryYearMonthListItemUi.Diary<DiaryDayListItemUi.WordSearchResult>
    ) {
        val listAdapter = createWordSearchResultDayListAdapter(holder)
        listAdapter.submitList(item.diaryDayList.itemList)
    }

    private fun createWordSearchResultDayListAdapter(
        holder: DiaryYearMonthListViewHolder.Item
    ): WordSearchResultDayListAdapter {
        val wordSearchResultDayListAdapter =
            WordSearchResultDayListAdapter(holder.binding.recyclerDayList, themeColor)
        return wordSearchResultDayListAdapter.apply {
            build()
            registerOnItemClickListener {
                onChildItemClickListener?.onClick(it)
            }
        }
    }

    private class DiffUtilItemCallback :
        DiaryYearMonthListBaseAdapter.DiffUtilItemCallback<DiaryDayListItemUi.WordSearchResult>() {

        private val logTag = createLogTag()

        override fun areContentsTheSame(
            oldItem: DiaryYearMonthListItemUi<DiaryDayListItemUi.WordSearchResult>,
            newItem: DiaryYearMonthListItemUi<DiaryDayListItemUi.WordSearchResult>
        ): Boolean {
            val result =
                when (oldItem) {
                    is DiaryYearMonthListItemUi.Diary -> {
                        if (newItem !is DiaryYearMonthListItemUi.Diary<DiaryDayListItemUi.WordSearchResult>) {
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
